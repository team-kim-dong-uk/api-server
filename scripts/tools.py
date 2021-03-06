import argparse  # to parse cli argument
import json  # to load data
from urllib import request  # for api request
import hashlib  # to calculate hash (md5)
import copy  # to deepcopy
import time  # to get current milliseconds
import jwt
from PIL import Image
from tqdm import tqdm


def main():
    parser = argparse.ArgumentParser(description="UDHD - cli tool for file upload")
    parser.add_argument('tool', metavar='<tool>',
                        help="currently support `upload`, `auth`")

    parser.add_argument('--data', type=str, default='data.json', metavar="<filename>",
                        help='data json file for upload files (default: data.json)')
    parser.add_argument('--meta', type=str, default="metadata.json", metavar='<filename>',
                        help="metadata json file (default: metadata.json")
    parser.add_argument('--jwt-secret', type=str, metavar='<jwt_secret>',
                        help="only for `auth` command")
    parser.add_argument('-v', '--verbose', default=False, type=str2bool,
                        help="print debug message")

    args = parser.parse_args()
    if args.verbose:
        print_arguments(args)

    meta = read_meta(args.meta)
    meta['__filename__'] = args.meta  # It is used internally.
    if args.verbose:
        print_meta(meta)

    data = read_data(args.data)
    if args.verbose:
        print_data(data)

    if args.tool == 'upload':
        upload(meta, data)
    elif args.tool == 'auth':
        if args.jwt_secret is None:
            print("Please input <jwt_secret>")
            return
        publish_auth(meta, args.jwt_secret)
    else:
        print('does not support for ', args.tool)


def publish_auth(meta, jwt_secret):
    print('=== publish authorization ===')
    print('This command will be able to overwrite on ' + meta['__filename__'] + ' to save authorization')
    if not ask_yes('Do you understand and accept it?'):
        return

    user_id = str(input('Enter userId :').strip())
    expire_range = int(input('Enter expire range(milliseconds) :'))
    expired_at = round(time.time() * 1000) + expire_range

    payload = {
        "sub": user_id,
        "exp": expired_at
    }
    token = jwt.encode(payload, jwt_secret, algorithm="HS256")
    print("Access Token : " + token)

    if not ask_yes("Do you save it into " + meta['__filename__']):
        return

    with open(meta['__filename__'], 'w') as f:
        meta['auth'] = 'Bearer ' + token
        del meta['__filename__'] # It is only used internally. So delete it when to save.
        f.write(json.dumps(meta, indent=2))

    print('Success!')


def ask_yes(question_str):
    while True:
        reply = str(input(question_str + " (y/n) :").lower().strip())
        if reply == 'n':
            print('rejected')
            return False
        elif reply == 'y':
            break
        else:
            print('please enter y/n')
    return True


def upload(meta, data):
    preprocessed_data = []

    for info in data:
        preprocessed_info = copy.deepcopy(info)
        binary_data = load_binary(preprocessed_info)
        preprocessed_info['hash'] = get_hash(binary_data)

        preprocessed_data.append(preprocessed_info)


    batch_size = 100
    for i in tqdm(range(int(len(preprocessed_data) / batch_size))):
        try:
            polling_key, urls, photo_ids = get_presigned_url(meta['url'], meta['auth'],
                                              preprocessed_data[i * batch_size:(i+1) * batch_size])
            for j in tqdm(range(batch_size)):
                try:
                    if urls[j] is None:
                        update_tags(meta['url'], meta['auth'], photo_ids[j], preprocessed_data[batch_size * i + j]['tags'])
                        #print('tag upload' + photo_ids[j] + ' tags: ' + preprocessed_data[batch_size * i + j]['tags'])
                    else:
                        put_image(urls[j], load_binary(preprocessed_data[batch_size * i + j]))
                        preprocessed_data[batch_size * i + j]['url'] = urls[j]
                        res = check_upload_progress(polling_key=polling_key,
                                                    info=preprocessed_data[batch_size * i + j],
                                                    url=meta['url'],
                                                    auth=meta['auth'])
                        update_tags(meta['url'], meta['auth'], res["photoId"], preprocessed_data[batch_size * i + j]['tags'])
                        #print('tag upload' + res["photoId"] + ' tags: ' + preprocessed_data[batch_size * i + j]['tags'])
                except Exception as e:
                    print(e)

        except Exception as e:
            print(e)
            pass

def update_tags(url, auth, photoId, tags):
    headers = {
        'Authorization': auth,
        'Content-Type': 'application/json'
    }
    tag_url = url + '/api/v1/upload/photo/' + photoId + '/tags'
    req = request.Request(tag_url, method="PUT",
                          data=bytes(json.dumps({'propagate': True, 'tags': tags}), encoding='utf-8'),
                          headers=headers)
    res = request.urlopen(req)

# TODO : Unsafety. Does not check it works properly.
def put_image(presigned_url, binary):
    headers = {
        'Content-Type':'image/jpeg'
    }
    req = request.Request(presigned_url, method="PUT", data=binary, headers=headers)
    res = request.urlopen(req)


# TODO
def get_presigned_url(url, auth, info):
    target_url = url + '/api/v1/upload/presigned-url'
    headers = {
        'Authorization': auth,
        'Content-Type': 'application/json'
    }
    hashes = list(map(lambda x: x['hash'], info))
    req = request.Request(target_url,
                          data=bytes(json.dumps({'checksums': hashes}), encoding='utf-8'),
                          headers=headers,
                          method='POST')

    with request.urlopen(req) as res:
        raw_data = res.read()
        data = json.loads(raw_data.decode('utf-8'))
        urls = data["urls"]
        polling_key = data["pollingKey"]
        photoIds = data["photoIds"]

    return polling_key, urls, photoIds


def load_binary(info):
    data = None

    if "filename" in info:
        f = open(info['filename'], 'rb')
        data = f.read()
        f.close()
    elif "url" in info:
        data = Image.open(info['url']).tobytes()

    return data


# TODO
def check_upload_progress(polling_key, info, url, auth):
    target_url = url + '/api/v1/upload/presigned-url/' + polling_key + '/' + info['hash']
    headers = {'Authorization': auth}
    req = request.Request(target_url, headers=headers)
    with request.urlopen(req) as res:
        raw_data = res.read()
        data = json.loads(raw_data.decode('utf-8'))
        return data

    return None


def print_arguments(args):
    print("=== arguments info ===")
    print("meta : ", args.meta)
    print("data : ", args.data)
    print("tool : ", args.tool)


def print_meta(meta):
    print("=== meta info ===")
    print("auth : ", meta["auth"])


def print_data(data):
    print("=== data info ===")
    print("len(data) : ", len(data))


def get_hash(data):
    return hashlib.md5(data).hexdigest()


def read_meta(meta_filename):
    with open(meta_filename, 'r') as f:
        meta = json.load(f)

    # TODO: Check necessary meta data
    return meta


def read_data(data_filename):
    with open(data_filename, 'r') as f:
        data = json.load(f)

    # TODO: Check necessary data
    return data


def str2bool(v):
    if isinstance(v, bool):
        return v
    if v.lower() in ('yes', 'true', 't', 'y', '1'):
        return True
    elif v.lower() in ('no', 'false', 'f', 'n', '0'):
        return False
    else:
        raise argparse.ArgumentTypeError('Boolean value expected.')


if __name__ == '__main__':
    main()

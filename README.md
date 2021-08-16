# API Server

## 공통부분

## Key Features
- 프론트엔드로부터 받은 이미지 업로드, 검색, 저장, 수정 등의 요청을 처리하는 서버입니다
- BK Tree Server 및 DB 와 통신해 나에게 없는 사진 검색 결과를 보여줍니다
- Tag Server 와 통신해 이미지 추천 태그를 유저에게 보내줍니다
- Google Drive API 와 S3 SDK 를 사용해 이미지를 업로드합니다

## How To Use
1. application.properties 파일 작성 (application.properties.example 파일을 참고)
1. `./gradlew build`
1. `./gradlew bootRun`

## API Documentation
http://udhd.djbaek.com:8080/docs/api-doc.html

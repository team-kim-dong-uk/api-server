package com.udhd.apiserver.domain.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@NoArgsConstructor
@Builder
@AllArgsConstructor
@Document(collection = "user")
public class User {

    @Id
    private ObjectId id;
    private String email;
    private String nickname;
    private String refreshToken;

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}

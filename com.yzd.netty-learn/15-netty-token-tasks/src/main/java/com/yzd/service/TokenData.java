package com.yzd.service;

import lombok.Data;

/**
 * @Author: yaozh
 * @Description:
 */
//@Getter
//@Setter
@Data
public class TokenData {
    public TokenData() {

    }

    public TokenData(String token) {
        this.token = token;
    }

    private String id;
    private String token;
}

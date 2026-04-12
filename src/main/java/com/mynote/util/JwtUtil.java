package com.mynote.util;


import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;


@Component
@Slf4j
public class JwtUtil {
    @Value("${jwt.secret}")
    private String secret;//密钥

    @Value("${jwt.expiration}")
    private Long expiration;//过期时间

    //获取签名密钥
    private Key getSigningKey() {
        //创建密钥
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
    //生成token
    public String generateToken(Long userId) {
        return Jwts.builder()
                .setSubject(userId.toString())//设置用户id
                .setIssuedAt(new Date())//记录签发时间
                .setExpiration(new Date(System.currentTimeMillis() + expiration))//设置过期时间
                .signWith(getSigningKey())//签名
                .compact();//compact()压缩生成token
    }
    //解析token
    public Long parseToken(String token) {
        try {
            return Long.parseLong(Jwts.parserBuilder()//解析token
                    .setSigningKey(getSigningKey())//设置密钥
                    .build()//构建解析器
                    .parseClaimsJws(token)//解析并验证token
                    .getBody()//获取载荷，数据部分
                    .getSubject());//获取用户id
        } catch (ExpiredJwtException e) {
            throw new RuntimeException("token过期，请重新登录");
        } catch (MalformedJwtException e) {
            throw new RuntimeException("Token格式错误");
        } catch (SignatureException e) {
            throw new RuntimeException("Token签名验证失败，Token无效");
        } catch (Exception e) {
            throw new RuntimeException("无效token");
        }
    }

    // 验证 Token 是否有效（不抛异常）
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}

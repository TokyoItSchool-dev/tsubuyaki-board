package com.example.tsubuyaki.web.dto;

import com.example.tsubuyaki.domain.AvatarColor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PostFormOptions {

    public List<String> avatarColors() {
        return AvatarColor.names();
    }
}

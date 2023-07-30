package com.example.cleancode.image.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@Data
public class MelonDTO {
    Long songId;
    Long albumId;
    String uri;
    String title;
    String singer;
    String genre;
}

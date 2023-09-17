package com.example.cleancode.user.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.example.cleancode.song.repository.ChartRepository;
import com.example.cleancode.user.JpaRepository.UserRepository;
import com.example.cleancode.user.JpaRepository.UserSongRepository;
import com.example.cleancode.user.dto.UserDto;
import com.example.cleancode.user.entity.User;
import com.example.cleancode.user.entity.UserSong;
import com.example.cleancode.utils.Role;
import com.example.cleancode.utils.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final JwtService jwtService;
    private final UserRepository memberRepository;
    private final UserSongRepository userSongRepository;
    private final ChartRepository chartRepository;
    private final AmazonS3 amazonS3;
    @Value("${cloud.aws.s3.bucket}")
    private String bucket;
    @Value("${file.dir}")
    private String fileDir;
    public UserDto findMember(Long id){
        log.info(id.toString());
        Optional<User> mem = memberRepository.findById(id);
        User member = mem.get();
        if(mem.isEmpty()) return null;
        return member.toMemberDto();
    }

    public boolean updateUser(UserDto memberDto, Long id){
        UserDto member = findMember(id);
        memberDto.setId(member.getId());
        memberDto.setRole(Role.ROLE_USER);
        try {
            memberRepository.save(memberDto.makeMember());
            return true;
        }catch (Exception e){
            return false;
        }
    }
    @Transactional
    public boolean userFile(String folder, MultipartFile multipartFile,Long id){
        String originalFilename = multipartFile.getOriginalFilename();
        User member = memberRepository.findById(id).get();
        if(member==null){
            return false;
        }

        String filename = folder+"/"+id+"_"+originalFilename;
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(multipartFile.getSize());
        metadata.setContentType(multipartFile.getContentType());
        UserSong userSong = UserSong.builder()
                .awsUrl(amazonS3.getUrl(bucket,filename).toString())
                .member(member)
                .build();
        try{
            amazonS3.putObject(bucket,filename,multipartFile.getInputStream(),metadata);
            userSongRepository.save(userSong);
        }catch (IOException ex){
            throw new RuntimeException();
        }catch (com.amazonaws.SdkClientException ex){
            throw new RuntimeException();
        }
        return true;
    }
    public List<UserSong> userFileList(Long id){
        return userSongRepository.findByMemberId(id);
    }

    @Transactional(rollbackFor = IllegalArgumentException.class)
    public boolean changeSelectList(List<Long> song,Long id){
        UserDto memberDto = memberRepository.findById(id).get().toMemberDto();
        if(memberDto==null) return false;
        memberDto.setSelected(song);
        memberRepository.save(memberDto.makeMember());
        return true;
    }
}

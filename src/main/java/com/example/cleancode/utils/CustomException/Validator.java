package com.example.cleancode.utils.CustomException;

import com.example.cleancode.song.entity.Song;
import com.example.cleancode.song.repository.SongRepository;
import com.example.cleancode.user.JpaRepository.UserRepository;
import com.example.cleancode.user.JpaRepository.UserSongRepository;
import com.example.cleancode.user.entity.User;
import com.example.cleancode.user.entity.UserSong;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class Validator {
    private final UserRepository userRepository;
    private final SongRepository songRepository;
    private final UserSongRepository userSongRepository;
    public User userValidator(Long userId){
        Optional<User> optionalUser = userRepository.findById(userId);
        if(optionalUser.isEmpty()){
            throw new NoUserException(ExceptionCode.USER_INVALID);
        }
        return optionalUser.get();
    }
    public UserSong userSongValidator(Long songId, Long userId){

        Optional<UserSong> optionalUserSong = userSongRepository.findByUserIdAndSongId(songId,userId);
        if(optionalUserSong.isEmpty()){
            throw new NoUserSongException(ExceptionCode.USER_SONG_INVALID);
        }
        return optionalUserSong.get();
    }
    public Song songValidator(Long songId){
        Optional<Song> optionalSong = songRepository.findById(songId);
        if(optionalSong.isEmpty()){
            throw new NoSongException(ExceptionCode.SONG_INVALID);
        }
        return optionalSong.get();
    }
}

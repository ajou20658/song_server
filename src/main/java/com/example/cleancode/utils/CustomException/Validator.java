package com.example.cleancode.utils.CustomException;

import com.example.cleancode.ddsp.entity.PtrData;
import com.example.cleancode.ddsp.entity.ResultSong;
import com.example.cleancode.ddsp.repository.PtrDataRepository;
import com.example.cleancode.ddsp.repository.ResultSongRepository;
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
    private final PtrDataRepository ptrDataRepository;
    private final ResultSongRepository resultSongRepository;

    public User userValidator(Long userId){
        Optional<User> optionalUser = userRepository.findById(userId);
        if(optionalUser.isEmpty()){
            throw new NoUserException(ExceptionCode.USER_INVALID);
        }
        return optionalUser.get();
    }
    public UserSong userSongValidator(Long songId, Long userId){
        Optional<UserSong> optionalUserSong = userSongRepository.findBySongIdAndUserId(songId,userId);
        log.info("UserSong Info : {}",optionalUserSong);
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
    public PtrData ptrDataValidator(Long ptrDataId){
        Optional<PtrData> optionalPtrData = ptrDataRepository.findById(ptrDataId);
        if(optionalPtrData.isEmpty()){
            throw new NoPtrException(ExceptionCode.PTR_ERROR);
        }
        return optionalPtrData.get();
    }
    public ResultSong resultSongValidator(Integer resultSongId){
        Optional<ResultSong> optionalResultSong = resultSongRepository.findById(resultSongId);
        if(optionalResultSong.isEmpty()){
            throw new NoGeneratedSongException(ExceptionCode.RESULT_SONG_ERROR);
        }
        return optionalResultSong.get();
    }
}

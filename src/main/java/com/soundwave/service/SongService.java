package com.soundwave.service;

import com.soundwave.model.Song;
import com.soundwave.repository.SongRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

@Service
@Slf4j
public class SongService {

    private final SongRepository songRepository;

    @Value("${soundwave.upload.dir:uploads}")
    private String uploadDir;

    private static final List<String> ALLOWED_TYPES = Arrays.asList(
        "audio/mpeg", "audio/mp3", "audio/wav", "audio/ogg", "audio/flac",
        "audio/aac", "audio/m4a", "audio/x-m4a", "audio/mp4", "audio/webm",
        "audio/x-wav", "audio/x-flac", "audio/opus", "audio/3gpp", "audio/amr"
    );

    private static final String[] COVER_COLORS = {
        "#FF6B6B", "#4ECDC4", "#45B7D1", "#96CEB4", "#FFEAA7",
        "#DDA0DD", "#98D8C8", "#F7DC6F", "#BB8FCE", "#85C1E9",
        "#F1948A", "#82E0AA", "#F8C471", "#AED6F1", "#A9DFBF",
        "#FAD7A0", "#D2B4DE", "#A3E4D7", "#FADBD8", "#D5DBDB"
    };

    public SongService(SongRepository songRepository) {
        this.songRepository = songRepository;
    }

    private Path getUploadPath() throws IOException {
        Path path = Paths.get(uploadDir).toAbsolutePath();
        if (!Files.exists(path)) {
            Files.createDirectories(path);
        }
        return path;
    }

    public Song uploadSong(MultipartFile file, String title, String artist,
                           String album, String genre, String uploaderName,
                           String description, String tags) throws IOException {

        // Validate file
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType.toLowerCase())) {
            // Try by extension fallback
            String name = file.getOriginalFilename();
            if (name != null && !isAudioByExtension(name)) {
                throw new IllegalArgumentException("Only audio files are allowed.");
            }
        }

        // Generate unique file name
        String ext = getExtension(file.getOriginalFilename());
        String uniqueFileName = UUID.randomUUID().toString() + "." + ext;

        // Save file to disk
        Path uploadPath = getUploadPath();
        Path filePath = uploadPath.resolve(uniqueFileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        log.info("Saved file: {} ({} bytes)", uniqueFileName, file.getSize());

        // Random cover color
        String color = COVER_COLORS[new Random().nextInt(COVER_COLORS.length)];

        // Build and save entity
        Song song = Song.builder()
            .title(title == null || title.isBlank() ? file.getOriginalFilename() : title)
            .artist(artist == null || artist.isBlank() ? "Unknown Artist" : artist)
            .album(album == null || album.isBlank() ? null : album)
            .genre(genre == null || genre.isBlank() ? "Other" : genre)
            .fileName(uniqueFileName)
            .originalFileName(file.getOriginalFilename())
            .fileSize(file.getSize())
            .contentType(contentType != null ? contentType : "audio/mpeg")
            .playCount(0L)
            .likeCount(0L)
            .uploaderName(uploaderName == null || uploaderName.isBlank() ? "Anonymous" : uploaderName)
            .coverColor(color)
            .description(description)
            .tags(tags)
            .build();

        return songRepository.save(song);
    }

    public byte[] streamSong(Long id) throws IOException {
        Song song = getSongById(id);
        Path filePath = getUploadPath().resolve(song.getFileName());
        if (!Files.exists(filePath)) {
            throw new NoSuchFileException("Audio file not found on disk: " + song.getFileName());
        }
        return Files.readAllBytes(filePath);
    }

    public Song incrementPlay(Long id) {
        Song song = getSongById(id);
        song.incrementPlayCount();
        return songRepository.save(song);
    }

    public Song toggleLike(Long id, boolean like) {
        Song song = getSongById(id);
        if (like) song.incrementLikeCount();
        else song.decrementLikeCount();
        return songRepository.save(song);
    }

    public Song getSongById(Long id) {
        return songRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Song not found: " + id));
    }

    public Page<Song> search(String query, int page, int size) {
        PageRequest pr = PageRequest.of(page, size, Sort.by("playCount").descending());
        if (query == null || query.isBlank()) {
            return songRepository.findAll(pr);
        }
        return songRepository.search(query.trim(), pr);
    }

    public List<Song> getTrending() {
        return songRepository.findTop20ByOrderByPlayCountDesc();
    }

    public List<Song> getMostLiked() {
        return songRepository.findTop20ByOrderByLikeCountDesc();
    }

    public List<Song> getRecentUploads() {
        return songRepository.findTop20ByOrderByUploadedAtDesc();
    }

    public List<Song> getRandomPicks() {
        return songRepository.findRandom(PageRequest.of(0, 10));
    }

    public List<Object[]> getGenreStats() {
        return songRepository.countByGenre();
    }

    public void deleteSong(Long id) throws IOException {
        Song song = getSongById(id);
        Path filePath = getUploadPath().resolve(song.getFileName());
        Files.deleteIfExists(filePath);
        songRepository.delete(song);
    }

    private String getExtension(String fileName) {
        if (fileName == null) return "mp3";
        int dot = fileName.lastIndexOf('.');
        return dot >= 0 ? fileName.substring(dot + 1).toLowerCase() : "mp3";
    }

    private boolean isAudioByExtension(String name) {
        String lower = name.toLowerCase();
        return lower.endsWith(".mp3") || lower.endsWith(".wav") || lower.endsWith(".ogg") ||
               lower.endsWith(".flac") || lower.endsWith(".aac") || lower.endsWith(".m4a") ||
               lower.endsWith(".opus") || lower.endsWith(".webm") || lower.endsWith(".3gp") ||
               lower.endsWith(".amr");
    }
}

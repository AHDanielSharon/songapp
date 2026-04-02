package com.soundwave.controller;

import com.soundwave.model.Song;
import com.soundwave.service.SongService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@RestController
@RequestMapping("/api/songs")
@CrossOrigin(origins = "*")
@Slf4j
public class SongController {

    private final SongService songService;

    public SongController(SongService songService) {
        this.songService = songService;
    }

    /** Upload a song */
    @PostMapping("/upload")
    public ResponseEntity<?> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "artist", required = false) String artist,
            @RequestParam(value = "album", required = false) String album,
            @RequestParam(value = "genre", required = false) String genre,
            @RequestParam(value = "uploaderName", required = false) String uploaderName,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "tags", required = false) String tags) {
        try {
            Song song = songService.uploadSong(file, title, artist, album, genre,
                                               uploaderName, description, tags);
            return ResponseEntity.ok(toDto(song));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Upload failed", e);
            return ResponseEntity.status(500).body(Map.of("error", "Upload failed: " + e.getMessage()));
        }
    }

    /** Stream audio file */
    @GetMapping("/{id}/stream")
    public ResponseEntity<byte[]> stream(@PathVariable Long id) {
        try {
            songService.incrementPlay(id);
            byte[] audio = songService.streamSong(id);
            Song song = songService.getSongById(id);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(
                song.getContentType() != null ? song.getContentType() : "audio/mpeg"));
            headers.setContentLength(audio.length);
            headers.set("Accept-Ranges", "bytes");
            headers.set("Content-Disposition", "inline; filename=\"" + song.getOriginalFileName() + "\"");
            return ResponseEntity.ok().headers(headers).body(audio);
        } catch (Exception e) {
            log.error("Stream error", e);
            return ResponseEntity.status(404).build();
        }
    }

    /** Search songs */
    @GetMapping("/search")
    public ResponseEntity<?> search(
            @RequestParam(value = "q", required = false) String query,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        Page<Song> result = songService.search(query, page, size);
        Map<String, Object> resp = new HashMap<>();
        resp.put("songs", result.getContent().stream().map(this::toDto).toList());
        resp.put("totalElements", result.getTotalElements());
        resp.put("totalPages", result.getTotalPages());
        resp.put("page", result.getNumber());
        return ResponseEntity.ok(resp);
    }

    /** Get single song */
    @GetMapping("/{id}")
    public ResponseEntity<?> getOne(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(toDto(songService.getSongById(id)));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    /** Trending songs */
    @GetMapping("/trending")
    public ResponseEntity<?> trending() {
        return ResponseEntity.ok(songService.getTrending().stream().map(this::toDto).toList());
    }

    /** Most liked */
    @GetMapping("/liked")
    public ResponseEntity<?> mostLiked() {
        return ResponseEntity.ok(songService.getMostLiked().stream().map(this::toDto).toList());
    }

    /** Recent uploads */
    @GetMapping("/recent")
    public ResponseEntity<?> recent() {
        return ResponseEntity.ok(songService.getRecentUploads().stream().map(this::toDto).toList());
    }

    /** Random picks */
    @GetMapping("/discover")
    public ResponseEntity<?> discover() {
        return ResponseEntity.ok(songService.getRandomPicks().stream().map(this::toDto).toList());
    }

    /** Genre stats */
    @GetMapping("/genres")
    public ResponseEntity<?> genres() {
        List<Map<String, Object>> list = songService.getGenreStats().stream().map(row -> {
            Map<String, Object> m = new HashMap<>();
            m.put("genre", row[0]);
            m.put("count", row[1]);
            return m;
        }).toList();
        return ResponseEntity.ok(list);
    }

    /** Like a song */
    @PostMapping("/{id}/like")
    public ResponseEntity<?> like(@PathVariable Long id,
                                   @RequestParam(defaultValue = "true") boolean like) {
        try {
            return ResponseEntity.ok(toDto(songService.toggleLike(id, like)));
        } catch (Exception e) {
            return ResponseEntity.status(404).body(Map.of("error", "Song not found"));
        }
    }

    /** Delete a song */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            songService.deleteSong(id);
            return ResponseEntity.ok(Map.of("message", "Deleted"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    /** Health check */
    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok(Map.of("status", "ok", "service", "SoundWave API"));
    }

    // Convert entity to DTO map
    private Map<String, Object> toDto(Song s) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", s.getId());
        m.put("title", s.getTitle());
        m.put("artist", s.getArtist());
        m.put("album", s.getAlbum());
        m.put("genre", s.getGenre());
        m.put("fileSize", s.getFileSize());
        m.put("contentType", s.getContentType());
        m.put("playCount", s.getPlayCount());
        m.put("likeCount", s.getLikeCount());
        m.put("uploadedAt", s.getUploadedAt());
        m.put("uploaderName", s.getUploaderName());
        m.put("coverColor", s.getCoverColor());
        m.put("description", s.getDescription());
        m.put("tags", s.getTags());
        m.put("streamUrl", "/api/songs/" + s.getId() + "/stream");
        return m;
    }
}

package com.soundwave.repository;

import com.soundwave.model.Song;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SongRepository extends JpaRepository<Song, Long> {

    // Full-text search across title, artist, album, genre, tags
    @Query("SELECT s FROM Song s WHERE " +
           "LOWER(s.title) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(s.artist) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(s.album) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(s.genre) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(s.tags) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(s.uploaderName) LIKE LOWER(CONCAT('%', :q, '%'))")
    Page<Song> search(@Param("q") String query, Pageable pageable);

    // Top trending (most played)
    List<Song> findTop20ByOrderByPlayCountDesc();

    // Most liked
    List<Song> findTop20ByOrderByLikeCountDesc();

    // Recently uploaded
    List<Song> findTop20ByOrderByUploadedAtDesc();

    // By genre
    Page<Song> findByGenreIgnoreCase(String genre, Pageable pageable);

    // By artist
    Page<Song> findByArtistIgnoreCaseContaining(String artist, Pageable pageable);

    // Count by genre
    @Query("SELECT s.genre, COUNT(s) FROM Song s WHERE s.genre IS NOT NULL GROUP BY s.genre ORDER BY COUNT(s) DESC")
    List<Object[]> countByGenre();

    // Random picks
    @Query("SELECT s FROM Song s ORDER BY RAND()")
    List<Song> findRandom(Pageable pageable);
}

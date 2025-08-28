package com.example.blog_be_springboot.repository;

import com.example.blog_be_springboot.entity.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {
    boolean existsByName(String name);

    List<Tag> searchTagsByName(String name);

    Page<Tag> searchTagsByNameContainingIgnoreCase(String keyword, Pageable pageable);
}

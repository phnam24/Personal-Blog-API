package com.example.blog_be_springboot.repository;

import com.example.blog_be_springboot.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {
}

package com.linkedin.post_service.Repository;

import com.linkedin.post_service.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post,String> {

    Optional<List<Post>> findByAuthorId(String userId);
}

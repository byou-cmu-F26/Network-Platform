package com.linkedin.post_service.Repository;

import com.linkedin.post_service.entity.Like;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LikeRepository extends JpaRepository<Like,String> {
}

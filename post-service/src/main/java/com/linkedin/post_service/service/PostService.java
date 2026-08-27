package com.linkedin.post_service.service;

import com.linkedin.post_service.Repository.CommentRepository;
import com.linkedin.post_service.Repository.LikeRepository;
import com.linkedin.post_service.Repository.PostRepository;
import com.linkedin.post_service.entity.Comment;
import com.linkedin.post_service.entity.Like;
import com.linkedin.post_service.entity.Post;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;
    private final S3Service s3Service;
    private final KafkaTemplate<String,Object> kafkaTemplate;
    private final String POST_CREATED_TOPIC="post.created";
    private final String POST_LIKED_TOPIC="post.liked";
    private final String POST_COMMENTED_TOPIC="post.commented";


    public Post createPost(String authorId, String content, MultipartFile image) {
        Post post = new Post();
        post.setAuthorId(authorId);
        post.setContent(content);

        if (image!=null&&!image.isEmpty()){
            String url = s3Service.uploadFile(image,"posts/"+authorId);
            post.setImageUrl(url);
        }

        Post savedPost=postRepository.save(post);

        Map<String,Object> postCreatedEvent= new HashMap<>();
        postCreatedEvent.put("postId",savedPost.getId());
        postCreatedEvent.put("authorId",savedPost.getAuthorId());
        postCreatedEvent.put("content",savedPost.getContent());
        postCreatedEvent.put("imageUrl",savedPost.getImageUrl());
        postCreatedEvent.put("createdAt",savedPost.getCreateAt());

        kafkaTemplate.send(POST_CREATED_TOPIC,savedPost.getId(),postCreatedEvent);
        return savedPost;
    }


    // 自己做的
    public String likePost(String postId, String userId) {
        int count;

        Post post = postRepository.findById(postId).orElseThrow(
                ()-> new RuntimeException("post does not exist")
        );

        if (likeRepository.existsByUserIdAndPostId(userId,postId)){
            count = post.getLikeCount()-1;
            post.setLikeCount(count);
            postRepository.save(post);
            likeRepository.deleteByUserIdAndPostId(userId,postId);
            return "Unliked";
        }else{
            count = post.getLikeCount()+1;
            post.setLikeCount(count);
            Post savedPost=postRepository.save(post);
            Like like = new Like();
            like.setPostId(postId);
            like.setUserId(userId);
            likeRepository.save(like);

            Map<String,Object> postLikedEvent= new HashMap<>();
            postLikedEvent.put("postId",savedPost.getId());
            postLikedEvent.put("authorId",savedPost.getAuthorId());
            postLikedEvent.put("userId",userId);
            kafkaTemplate.send(POST_LIKED_TOPIC,savedPost.getId(),postLikedEvent);
            return "Liked";
        }
    }


    public Post getPost(String postId) {
        Post post = postRepository.findById(postId).orElseThrow(
                ()->new RuntimeException("post does not exist")
        );
        return post;
    }


    public List<Post> getUserPosts(String userId) {
        List<Post> postList = postRepository.findByAuthorId(userId).orElseThrow(
                ()->new RuntimeException("according to userId no posts founded error")
        );
        return postList;
    }


    public Comment addComment(String postId, String authorId, String content) {
        Comment comment = new Comment();

        comment.setPostId(postId);
        comment.setAuthorId(authorId);
        comment.setContent(content);

        Comment savedComment=commentRepository.save(comment);

        Post post= postRepository.getById(postId);
        post.setCommentCount(post.getCommentCount()+1);
        Post savedPost=postRepository.save(post);

        Map <String,Object> commentAddedEvent = new HashMap<>();
        commentAddedEvent.put("postAuthorId",post.getAuthorId());
        commentAddedEvent.put("postId",savedComment.getPostId());
        commentAddedEvent.put("authorId",savedComment.getAuthorId());
        commentAddedEvent.put("commentId",savedComment.getId());

        kafkaTemplate.send(POST_COMMENTED_TOPIC,savedPost.getId(),commentAddedEvent);

        return savedComment;
    }


    public List<Comment> getComments(String postId) {
        List<Comment> comments= commentRepository.findByPostIdOrderByCreatedAtDesc(postId).orElseThrow(
                ()->new RuntimeException("no post found"));
        return comments;
    }


    public String deletePost(String postId, String userId) {
        Post post = getPost(postId);
        if (!post.getAuthorId().equals(userId)){
            new RuntimeException("Not Authorized to delete this post");
        }
        postRepository.delete(post);
        return "post deleted";
    }
}

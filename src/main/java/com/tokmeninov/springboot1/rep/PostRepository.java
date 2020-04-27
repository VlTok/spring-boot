package com.tokmeninov.springboot1.rep;

import com.tokmeninov.springboot1.models.Post;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface PostRepository extends CrudRepository<Post, Long> {

    List<Post> findByTitle(String title);
}

package com.tokmeninov.springboot1.controllers;

import com.tokmeninov.springboot1.models.Post;
import com.tokmeninov.springboot1.models.User;
import com.tokmeninov.springboot1.rep.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.util.*;

@Controller
public class BlogController {

    @Autowired
    private PostRepository postRepository;

    @Value("${upload.path}")
    private String uploadPath;

    @GetMapping("/blog")
    public String blog(@RequestParam(required = false , defaultValue = "") String filter, Model model){
        Iterable<Post> posts = postRepository.findAll();

        if (filter != null && !filter.isEmpty()) {
            posts = postRepository.findByTitle(filter);
        } else {
            posts =  postRepository.findAll();
        }

        model.addAttribute("posts",posts);
        model.addAttribute("filter",filter);

        return "blog-main";
    }

    @GetMapping("/blog/add")
    public String blogAdd(Model model){
        return "blog-add";
    }

    @PostMapping("/blog/add")
    public String blogPostAdd(@AuthenticationPrincipal User user,
                              @Valid Post post,
                              BindingResult bindingResult,
                              Model model,
                              @RequestParam("file") MultipartFile file) throws IOException {
        post.setAuthor(user);

        if(bindingResult.hasErrors()){
            Map<String, String> errorsMap = ControllerUtils.getErrors(bindingResult);
            model.mergeAttributes(errorsMap);
            model.addAttribute("message",post);
            return "blog-add";
        }else {
            if (file != null && !file.getOriginalFilename().isEmpty()) {
                File uploadDir = new File(uploadPath);

                if (!uploadDir.exists()) {
                    uploadDir.mkdir();
                }

                String uuidFile = UUID.randomUUID().toString();
                String resultFilename = uuidFile + "." + file.getOriginalFilename();

                file.transferTo(new File(uploadPath + "/" + resultFilename));

                post.setFilename(resultFilename);
            }
            model.addAttribute("message", null);

            postRepository.save(post);
        }

        return "redirect:/blog";
    }

    @GetMapping("/blog/{id}")
    public String blogDetails(@PathVariable(value = "id") long id, Model model){
        if(!postRepository.existsById(id)){
            return "redirect:/blog";
        }
        Optional <Post> post= postRepository.findById(id);
        ArrayList <Post> res= new ArrayList<>();
        post.ifPresent(res::add);
        model.addAttribute("post", res);
        return "blog-details";
    }


    @GetMapping("/blog/{id}/edit")
    public String blogEdit(@PathVariable(value = "id") long id, Model model){
        if(!postRepository.existsById(id)){
            return "redirect:/blog";
        }
        Optional <Post> post= postRepository.findById(id);
        ArrayList <Post> res= new ArrayList<>();
        post.ifPresent(res::add);
        model.addAttribute("post", res);
        return "blog-edit";
    }

    @PostMapping("/blog/{id}/edit")
    public String blogPostUpdate(@PathVariable(value="id") long id, @RequestParam String  title,@RequestParam String  anons,@RequestParam String  full_text, Model model){
        Post post = postRepository.findById(id).orElseThrow();
        post.setTitle(title);
        post.setAnons(anons);
        post.setFull_text(full_text);

        postRepository.save(post);

        return "redirect:/blog";
    }

    @PostMapping("/blog/{id}/remove")
    public String blogPostDelete(@PathVariable(value="id") long id, Model model){
        Post post = postRepository.findById(id).orElseThrow();
        postRepository.delete(post);

        return "redirect:/blog";
    }

    @GetMapping("/user-posts/{user}")
    public String userMessages(
            @PathVariable User user,
            Model model
    ){
        Set<Post> posts=user.getPosts();
        model.addAttribute("username",user.getUsername());
        model.addAttribute("posts",posts);

        return "user-posts";
    }
}

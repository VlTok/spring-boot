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
            saveFile(post,file);
            model.addAttribute("message", null);

            postRepository.save(post);
        }

        return "redirect:/blog";
    }

    private void saveFile(@Valid Post post, @RequestParam("file") MultipartFile file) throws IOException {
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
    }

    @GetMapping("/blog/{post}")
    public String blogDetails(@AuthenticationPrincipal User currentUser,@PathVariable Post post, Model model){
        if(!postRepository.existsById(post.getId())){
            return "redirect:/blog";
        }
        Optional <Post> user_post= postRepository.findById(post.getId());
        ArrayList <Post> res= new ArrayList<>();
        user_post.ifPresent(res::add);
        model.addAttribute("post", res);
        model.addAttribute("isCurrentUser",currentUser.equals(post.getAuthor()));
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

    @PostMapping("/blog/{post}/edit")
    public String blogPostUpdate(@AuthenticationPrincipal User currentUser,
                                 @PathVariable Post post,
                                 @RequestParam String  title,
                                 @RequestParam String  anons,
                                 @RequestParam String  full_text,
                                 @RequestParam MultipartFile file,
                                 Model model
    ) throws IOException {
        if (post.getAuthor().equals(currentUser)) {
            post.setTitle(title);
            post.setAnons(anons);
            post.setFull_text(full_text);

            saveFile(post, file);
            postRepository.save(post);
        }
        return "redirect:/blog";
    }

    @PostMapping("/blog/{post}/remove")
    public String blogPostDelete(@AuthenticationPrincipal User currentUser, @PathVariable Post post, Model model){
        if (post.getAuthor().equals(currentUser)) {
            postRepository.delete(post);
        }
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

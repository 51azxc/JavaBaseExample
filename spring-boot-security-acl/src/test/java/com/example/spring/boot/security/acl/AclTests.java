package com.example.spring.boot.security.acl;

import com.example.spring.boot.security.acl.domain.Post;
import com.example.spring.boot.security.acl.service.PostService;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
public class AclTests {
    @Autowired PostService postService;

    @Before
    public void setUp() {
        Post p1 = new Post("post 1", "admin's post", "admin");
        postService.savePost(p1);
        Post p2 = new Post("post 2", "user1's post", "user1");
        postService.savePost(p2);
        Post p3 = new Post("post 3", "user2's post", "user2");
        postService.savePost(p3);
    }

    @WithMockUser(username = "admin", roles = {"ADMIN", "USER"})
    @Test
    public void testAdminAccess() {
        Pageable pageable = PageRequest.of(0, 10);
        Assert.assertThat(postService.getPosts1(pageable).getContent().size(), CoreMatchers.is(3));
        postService.deletePost(1L);
        Assert.assertThat(postService.getPosts2(pageable).getContent().size(), CoreMatchers.is(2));
        postService.deletePost(2L);
        Assert.assertThat(postService.getPosts().size(), CoreMatchers.is(1));
    }

    @WithMockUser("user1")
    @Test
    public void testUserGetPosts() {
        List<Post> posts = postService.getPosts();
        Assert.assertThat(posts.size(), CoreMatchers.is(1));
        Assert.assertThat(posts.get(0).getAuthor(), CoreMatchers.is("user1"));
        Assert.assertThat(postService.getPost(2L).get().getAuthor(), CoreMatchers.is("user1"));
    }

    @WithMockUser("user2")
    @Test(expected = AccessDeniedException.class)
    public void testUserCannotGetPost() {
        Assert.assertThat(postService.getPost(2L).get().getAuthor(), CoreMatchers.is("user1"));
        Assert.assertThat(postService.getPost(3L).get().getAuthor(), CoreMatchers.is("user2"));
    }

    @WithMockUser("user1")
    @Test(expected = AccessDeniedException.class)
    public void testUserCannotDeletePost() {
        postService.deletePost(3L);
    }

    @WithMockUser("user2")
    @Test
    public void testUserDeletePost() {
        Assert.assertThat(postService.getPost(3L).get().getAuthor(), CoreMatchers.is("user2"));
        postService.deletePost(3L);
    }

}

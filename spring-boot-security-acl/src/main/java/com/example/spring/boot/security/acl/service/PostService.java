package com.example.spring.boot.security.acl.service;

import com.example.spring.boot.security.acl.domain.Post;
import com.example.spring.boot.security.acl.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.acls.domain.GrantedAuthoritySid;
import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.model.MutableAcl;
import org.springframework.security.acls.model.MutableAclService;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final MutableAclService aclService;

    @Autowired
    public PostService(PostRepository postRepository, MutableAclService aclService) {
        this.postRepository = postRepository;
        this.aclService = aclService;
    }

    @Transactional
    public void savePost(Post post) {
        Post p = postRepository.save(post);
        ObjectIdentity objectIdentity = new ObjectIdentityImpl(Post.class, p.getId());
        MutableAcl acl = aclService.createAcl(objectIdentity);
        PrincipalSid sid = new PrincipalSid(post.getAuthor());
        int index = acl.getEntries().size();
        acl.insertAce(index++, BasePermission.ADMINISTRATION, new GrantedAuthoritySid("ROLE_ADMIN"), true);
        acl.insertAce(index++, BasePermission.DELETE, sid, true);
        acl.insertAce(index++, BasePermission.READ, sid, true);
        aclService.updateAcl(acl);
    }

    public Optional<Post> getPost(Long id) {
        return postRepository.findById(id);
    }

    public List<Post> getPosts() {
        return postRepository.findAll();
    }

    public Page<Post> getPosts1(Pageable pageable) {
        List<Post> posts = getPosts();
        int start = (int)pageable.getOffset();
        int end = (start + pageable.getPageSize()) > posts.size() ? posts.size() : start + pageable.getPageSize();
        Page<Post> page = new PageImpl<>(posts.subList(start, end), pageable, posts.size());
        return  page;
    }

    public Page<Post> getPosts2(Pageable pageable) {
        return postRepository.findAll(pageable);
    }

    @Transactional
    public void deletePost(Long id) {
        postRepository.deleteById(id);
        ObjectIdentity objectIdentity = new ObjectIdentityImpl(Post.class, id);
        aclService.deleteAcl(objectIdentity, false);
        /*
        getPost(id).ifPresent(post -> {
            postRepository.delete(post);
            ObjectIdentity objectIdentity = new ObjectIdentityImpl(Post.class, id);
            aclService.deleteAcl(objectIdentity, false);
        });
        */
    }
}

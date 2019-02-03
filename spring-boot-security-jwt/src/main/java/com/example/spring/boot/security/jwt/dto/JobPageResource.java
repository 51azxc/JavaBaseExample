package com.example.spring.boot.security.jwt.dto;

import org.springframework.data.domain.Page;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;

//自定义hateoas resource
public class JobPageResource extends ResourceSupport {
    private final Page page;

    public JobPageResource(Page page, String pageParam, String sizeParam) {
        super();
        this.page = page;

        if (page.hasPrevious()) {
            add(buildPageLink(pageParam,page.getNumber()-1, sizeParam, Link.REL_PREVIOUS));
        }
        if (page.hasNext()) {
            add(buildPageLink(pageParam,page.getNumber()+1, sizeParam, Link.REL_NEXT));
        }
        add(buildPageLink(pageParam,0, sizeParam, Link.REL_FIRST));
        add(buildPageLink(pageParam, page.getTotalPages()-1, sizeParam, Link.REL_LAST));
        add(buildPageLink(pageParam, page.getNumber(), sizeParam, Link.REL_SELF));
    }

    private Link buildPageLink(String pageParam, int start, String sizeParam, String rel) {
        String path = uriComponentsBuilder()
                .queryParam(pageParam, start)
                .queryParam(sizeParam, page.getSize())
                .build().toString();
        Link link = new Link(path, rel);
        return link;
    }

    private ServletUriComponentsBuilder uriComponentsBuilder() {
        return ServletUriComponentsBuilder.fromCurrentRequestUri();
    }

    public int getNumber() {
        return page.getNumber();
    }
    public int getSize() {
        return page.getSize();
    }
    public int getTotalPages() {
        return page.getTotalPages();
    }
    public long getTotalElements() {
        return page.getTotalElements();
    }
    public int getNumberOfElements() {
        return page.getNumberOfElements();
    }

    public List<?> getContent() {
        return page.getContent();
    }
}

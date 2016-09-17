package de.kja.app.client;

import org.androidannotations.rest.spring.annotations.Body;
import org.androidannotations.rest.spring.annotations.Field;
import org.androidannotations.rest.spring.annotations.Get;
import org.androidannotations.rest.spring.annotations.Path;
import org.androidannotations.rest.spring.annotations.Post;
import org.androidannotations.rest.spring.annotations.Rest;
import org.androidannotations.rest.spring.api.RestClientErrorHandling;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import java.util.ArrayList;
import java.util.List;

import de.kja.app.Constants;
import de.kja.app.model.Comment;
import de.kja.app.model.Content;

@Rest(rootUrl = Constants.HOST, converters = {MappingJackson2HttpMessageConverter.class},
        interceptors = {Authenticator.class})
public interface ContentClient extends RestClientErrorHandling {

    @Get("/service/v1/content?district={district}&language={language}")
    ContentList getContents(@Path("district") String district, @Path("language") String language);

    @Get("/service/v1/content/{id}")
    CommentList getComments(@Path("id") long contentId);

    @Post("/service/v1/content/{id}")
    void postComment(@Path("id") long contentId, @Body String comment);

}

class ContentList extends ArrayList<Content> {}

class CommentList extends ArrayList<Comment> {}

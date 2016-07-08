package de.kja.app.client;

import org.androidannotations.rest.spring.annotations.Get;
import org.androidannotations.rest.spring.annotations.Rest;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import java.util.ArrayList;
import java.util.List;

import de.kja.app.model.Content;

@Rest(rootUrl = "http://192.168.1.23:8080", converters = {MappingJackson2HttpMessageConverter.class})
public interface ContentClient {


    @Get("/service/content")
    ContentList getContents();

}

class ContentList extends ArrayList<Content> {}

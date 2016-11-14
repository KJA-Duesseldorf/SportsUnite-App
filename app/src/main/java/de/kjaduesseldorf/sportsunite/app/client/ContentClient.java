package de.kjaduesseldorf.sportsunite.app.client;

import org.androidannotations.rest.spring.annotations.Get;
import org.androidannotations.rest.spring.annotations.Path;
import org.androidannotations.rest.spring.annotations.Rest;
import org.androidannotations.rest.spring.api.RestClientErrorHandling;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import java.util.ArrayList;

import de.kjaduesseldorf.sportsunite.app.Constants;
import de.kjaduesseldorf.sportsunite.app.model.Content;

@Rest(rootUrl = Constants.HOST, converters = {MappingJackson2HttpMessageConverter.class})
public interface ContentClient extends RestClientErrorHandling {

    @Get("/service/v1/content?district={district}&language={language}")
    ContentList getContents(@Path("district") String district, @Path("language") String language);

}

class ContentList extends ArrayList<Content> {}

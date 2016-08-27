package de.kja.app.client;

import org.androidannotations.rest.spring.annotations.Get;
import org.androidannotations.rest.spring.annotations.Rest;
import org.androidannotations.rest.spring.api.RestClientErrorHandling;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import java.util.ArrayList;

import de.kja.app.Constants;
import de.kja.app.model.District;

@Rest(rootUrl = Constants.HOST, converters = {MappingJackson2HttpMessageConverter.class})
public interface DistrictClient extends RestClientErrorHandling {

    @Get("/service/v1/district")
    DistrictList getValidDistricts();

}

class DistrictList extends ArrayList<District> {

}
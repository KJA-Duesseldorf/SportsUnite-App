package de.kja.app.client;

import org.androidannotations.rest.spring.annotations.Field;
import org.androidannotations.rest.spring.annotations.Get;
import org.androidannotations.rest.spring.annotations.Path;
import org.androidannotations.rest.spring.annotations.Post;
import org.androidannotations.rest.spring.annotations.Rest;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;

import de.kja.app.Constants;

@Rest(rootUrl = Constants.HOST, converters = {FormHttpMessageConverter.class, StringHttpMessageConverter.class})
public interface RegisterClient {

    @Post("/service/v1/register")
    void register(@Field("name") String name, @Field("password") String password);

    @Get("/service/v1/register?name={name}")
    String isUsed(@Path("name") String name);

}

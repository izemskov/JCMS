/* This Source Code Form is subject to the terms of the Mozilla
 * Public License, v. 2.0. If a copy of the MPL was not distributed
 * with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Copyright 2020 Ilya Zemskov */

package ru.develgame.jcms;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import ru.develgame.jcms.common.CommonFunctions;
import ru.develgame.jcms.common.StorageService;

@SpringBootApplication
@EnableCaching
public class JCMS {
    private static ConfigurableApplicationContext context;

    @Value("${catalogItemSavePath}")
    private String catalogItemSavePath;

    @Bean
    public StorageService storageService() {
        return new StorageService() {
            @Override
            public Resource loadAsResource(String path) {
                return context.getResource("file:" + catalogItemSavePath + path);
            }
        };
    }

    public static void main(String[] args) {
        context = SpringApplication.run(JCMS.class, args);
        context.getBean(CommonFunctions.class).checkCreds();
    }
}

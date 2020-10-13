/*
 *
 * This software is copyright protected (c) 2020 by S-Terra CSP
 *
 * Author:              Ilya Zemskov
 * E-mail:              izemskov@s-terra.com
 *
 * Owner:               Ilya Zemskov
 * E-mail:              izemskov@s-terra.com
 *
 * $Header: $
 *
 */
package ru.develgame.jcms.common;

import org.springframework.core.io.Resource;

public interface StorageService {
    Resource loadAsResource(String path);
}

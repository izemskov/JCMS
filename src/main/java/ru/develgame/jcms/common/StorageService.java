/* This Source Code Form is subject to the terms of the Mozilla
 * Public License, v. 2.0. If a copy of the MPL was not distributed
 * with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Copyright 2020 Ilya Zemskov */
package ru.develgame.jcms.common;

import org.springframework.core.io.Resource;

public interface StorageService {
    Resource loadAsResource(String path);
}

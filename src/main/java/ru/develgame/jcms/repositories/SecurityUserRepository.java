/* This Source Code Form is subject to the terms of the Mozilla
 * Public License, v. 2.0. If a copy of the MPL was not distributed
 * with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Copyright 2020 Ilya Zemskov */

package ru.develgame.jcms.repositories;

import org.springframework.data.repository.CrudRepository;
import ru.develgame.jcms.entities.SecurityUser;

import java.util.Optional;

public interface SecurityUserRepository extends CrudRepository<SecurityUser, Long> {
    Optional<SecurityUser> findById(Long id);
    SecurityUser findByName(String name);
}

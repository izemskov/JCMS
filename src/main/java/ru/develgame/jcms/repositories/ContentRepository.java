/* This Source Code Form is subject to the terms of the Mozilla
 * Public License, v. 2.0. If a copy of the MPL was not distributed
 * with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Copyright 2020 Ilya Zemskov */

package ru.develgame.jcms.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.develgame.jcms.entities.Content;

import java.util.List;
import java.util.Optional;

public interface ContentRepository extends JpaRepository<Content, Long> {
    List<Content> findByParentContentOrderByOrderContent(Content parentContent);
    Optional<Content> findTop1ByParentContentOrderByOrderContent(Content parentContent);
    Optional<Content> findByLink(String link);
}

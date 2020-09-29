/* This Source Code Form is subject to the terms of the Mozilla
 * Public License, v. 2.0. If a copy of the MPL was not distributed
 * with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Copyright 2020 Ilya Zemskov */

package ru.develgame.jcms.composers.admin;

import org.springframework.transaction.annotation.Transactional;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventQueue;
import org.zkoss.zk.ui.event.EventQueues;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.*;
import ru.develgame.jcms.CommonFunctions;
import ru.develgame.jcms.entities.SecurityUser;
import ru.develgame.jcms.renders.UsersRowRender;
import ru.develgame.jcms.repositories.SecurityUserRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class UsersComposer extends SelectorComposer {
    @WireVariable private SecurityUserRepository securityUserRepository;
    @WireVariable private CommonFunctions commonFunctions;

    @Wire private Grid usersGrid;
    @Wire private Button removeUserButton;

    private ListModel<SecurityUser> usersDataModel = null;

    private void refreshUsersDataModel() {
        usersDataModel = new ListModelList<>(securityUserRepository.findAll());
    }

    public ListModel<SecurityUser> getUsersDataModel() {
        if (usersDataModel == null) {
            refreshUsersDataModel();
        }

        return usersDataModel;
    }

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);

        removeUserButton.setDisabled(true);

        EventQueue<Event> eq = EventQueues.lookup("addEditUsers", EventQueues.DESKTOP, true);
        eq.subscribe(event -> {
            refreshUsersDataModel();
            usersGrid.setModel(usersDataModel);
        });

        EventQueue<org.zkoss.zk.ui.event.Event> eq1 = EventQueues.lookup("delUsers", EventQueues.DESKTOP, true);
        eq1.subscribe(event -> {
            if (event.getData().equals("enable"))
                removeUserButton.setDisabled(false);
            else
                removeUserButton.setDisabled(true);
        });
    }

    @Listen("onClick = #createUserButton")
    public void createUserButtonOnClick() {
        Map<String, String> args = new HashMap<>();
        Window window = (Window) Executions.createComponents(
                "~./admin/widgets/addEditUser.zul", null, args);
        window.doModal();
    }

    @Listen("onClick = #removeUserButton")
    public void removeUserButtonOnClick() {
        RowRenderer<SecurityUser> rowRenderer = usersGrid.getRowRenderer();
        List<SecurityUser> delTemplatesList = ((UsersRowRender) rowRenderer).getDelUsersList();

        delTemplatesList.forEach(t -> {

        });

        refreshUsersDataModel();
        usersGrid.setModel(usersDataModel);

        removeUserButton.setDisabled(true);
    }
}

/* This Source Code Form is subject to the terms of the Mozilla
 * Public License, v. 2.0. If a copy of the MPL was not distributed
 * with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Copyright 2020 Ilya Zemskov */

package ru.develgame.jcms.admin.composers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.zkoss.util.resource.Labels;
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
import ru.develgame.jcms.admin.renders.UsersRowRender;
import ru.develgame.jcms.common.CommonFunctions;
import ru.develgame.jcms.entities.SecurityUser;
import ru.develgame.jcms.repositories.SecurityUserRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class UsersComposer extends SelectorComposer {
    @WireVariable private SecurityUserRepository securityUserRepository;
    @WireVariable private CommonFunctions commonFunctions;
    @WireVariable private PlatformTransactionManager transactionManager;
    @WireVariable private SessionRegistry sessionRegistry;

    @Wire private Grid usersGrid;
    @Wire private Button removeUserButton;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private TransactionTemplate transactionTemplate = null;

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

    public TransactionTemplate getTransactionTemplate() {
        if (transactionTemplate == null)
            transactionTemplate = new TransactionTemplate(transactionManager);

        return transactionTemplate;
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
        List<SecurityUser> delUsersList = ((UsersRowRender) rowRenderer).getDelUsersList();

        int status = 0;
        try {
            getTransactionTemplate().execute(transactionStatus -> {
                securityUserRepository.deleteAll(delUsersList);

                for (Object principal : sessionRegistry.getAllPrincipals()) {
                    for (SessionInformation session : sessionRegistry.getAllSessions(principal, false)) {
                        session.expireNow();
                    }
                }

                return 0;
            });
        }
        catch (Exception ex) {
            logger.error("", ex);
            status = 1;
        }

        refreshUsersDataModel();
        usersGrid.setModel(usersDataModel);

        removeUserButton.setDisabled(true);

        if (status != 0) {
            Messagebox.show(Labels.getLabel("users.error.cannotRemove"),
                    null, 0,  Messagebox.ERROR);
            return;
        }
    }
}

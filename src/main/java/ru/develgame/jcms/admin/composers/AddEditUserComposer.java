/* This Source Code Form is subject to the terms of the Mozilla
 * Public License, v. 2.0. If a copy of the MPL was not distributed
 * with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Copyright 2020 Ilya Zemskov */

package ru.develgame.jcms.admin.composers;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventQueue;
import org.zkoss.zk.ui.event.EventQueues;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;
import ru.develgame.jcms.entities.SecurityUser;
import ru.develgame.jcms.repositories.SecurityUserRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class AddEditUserComposer extends SelectorComposer {
    @Wire private Window addEditUserForm;
    @Wire private Textbox nameTextBox;
    @Wire private Textbox passwordTextBox;

    @WireVariable
    private SecurityUserRepository securityUserRepository;

    private SecurityUser user = null;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);

        addEditUserForm.addEventListener("onCustomClose", event -> {
            addEditUserForm.detach();
        });

        String userIdStr = (String) Executions.getCurrent().getArg().get("userId");
        if (userIdStr != null && !userIdStr.isEmpty()) {
            try {
                long userId = Long.parseLong(userIdStr);
                Optional<SecurityUser> byId = securityUserRepository.findById(userId);
                if (!byId.isPresent()) {
                    Messagebox.show(Labels.getLabel("addEditUser.error.userNotFound"),
                            null, 0,  Messagebox.ERROR);
                    Events.postEvent("onCustomClose", addEditUserForm, null);
                    return;
                }
                user = byId.get();

                addEditUserForm.setTitle(Labels.getLabel("addEditUser.title.edit"));

                nameTextBox.setText(user.getName());
            }
            catch (NumberFormatException ex) {
                Messagebox.show(Labels.getLabel("addEditUser.error.userIdNan"),
                        null, 0,  Messagebox.ERROR);
                Events.postEvent("onCustomClose", addEditUserForm, null);
                return;
            }
        }
    }

    @Listen("onClick = #cancelButton")
    public void cancelOnClick() {
        addEditUserForm.detach();
    }

    @Listen("onClick = #okButton")
    public void okButtonOnClick() {
        if (nameTextBox.getText().isEmpty()) {
            Messagebox.show(Labels.getLabel("addEditContent.error.nameTextBoxEmpty"),
                    null, 0,  Messagebox.EXCLAMATION);
            return;
        }

        if (passwordTextBox.getText().isEmpty()) {
            Messagebox.show(Labels.getLabel("addEditContent.error.orderTextBoxEmpty"),
                    null, 0,  Messagebox.EXCLAMATION);
            return;
        }

        if (user == null)
            user = new SecurityUser();
        user.setName(nameTextBox.getText());
        user.setPass(new BCryptPasswordEncoder().encode(passwordTextBox.getText()));
        securityUserRepository.save(user);

        addEditUserForm.detach();

        String exclude = "";
        List<String> excludeLst = new ArrayList(Arrays.asList(exclude.split("\\s+")).stream().map(t -> t.toLowerCase()).collect(Collectors.toList()));

        EventQueue<Event> eq = EventQueues.lookup("addEditUsers", EventQueues.DESKTOP, true);
        eq.publish(new org.zkoss.zk.ui.event.Event("closeAddEditUsers", null, "finished"));
    }
}

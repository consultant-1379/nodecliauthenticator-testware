/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/

package com.ericsson.oss.services.nodecli.operators.views;

import java.util.List;

import com.ericsson.cifwk.taf.ui.core.SelectorType;
import com.ericsson.cifwk.taf.ui.core.UiComponent;
import com.ericsson.cifwk.taf.ui.core.UiComponentMapping;
import com.ericsson.cifwk.taf.ui.sdk.GenericViewModel;

/**
 * <pre>
 * <b>Name</b>: ActionBarViewModel      <i>[public (Class)]</i>
 * <b>Description</b>: This class contains the definitions and methods for graphical
 * management of the CLI through the UI.
 * </pre>
 */
public class ActionBarViewModel extends GenericViewModel {

    @UiComponentMapping(selector = ".elTerminal-terminal", selectorType = SelectorType.CSS)
    private UiComponent shell;
    @UiComponentMapping(selector = ".xterm-rows", selectorType = SelectorType.CSS)
    private UiComponent textArea;

    @UiComponentMapping(selector = ".ebDialog", selectorType = SelectorType.CSS)
    private UiComponent popup;

    @UiComponentMapping(selector = ".ebRadioBtn-label", selectorType = SelectorType.CSS)
    private List<UiComponent> radioButton;

    @UiComponentMapping(selector = ".ebBtn.ebBtn_color_darkBlue", selectorType = SelectorType.CSS)
    private UiComponent connectButton;

    /**
     * <pre>
     * <b>Name</b>: getShell            <i>[public]</i>
     * <b>Description</b>: .
     * </pre>
     *
     * @return - UiComponent for CLI shell.
     */
    public UiComponent getShell() {
        return shell;
    }

    /**
     * <pre>
     * <b>Name</b>: getTextArea            <i>[public]</i>
     * <b>Description</b>: .
     * </pre>
     *
     * @return - UiComponent for CLI shell.
     */
    public UiComponent getTextArea() {
        return textArea;
    }

    /**
     * <pre>
     * <b>Name</b>: getShell            <i>[public]</i>
     * <b>Description</b>: .
     * </pre>
     *
     * @return - UiComponent for popUp window.
     */
    public UiComponent getPopUp() {
        return popup;
    }

    /**
     * <pre>
     * <b>Name</b>: getShell            <i>[public]</i>
     * <b>Description</b>: .
     * </pre>
     *
     * @return - UiComponent for Connect Button.
     */
    public UiComponent getConnectButton() {
        return connectButton;
    }

    /**
     * <pre>
     * <b>Name</b>: getShell            <i>[public]</i>
     * <b>Description</b>: .
     * </pre>
     *
     * @param buttonName Name of Button to Use.
     */
    public void clickRadioButton(final String buttonName) {
        for (final UiComponent button : radioButton) {
            if (button.getText().contains(buttonName)) {
                button.click();
            }
        }
    }
}

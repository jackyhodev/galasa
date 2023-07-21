/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos3270.internal.gherkin;

import java.util.List;
import java.util.Map;

import dev.galasa.framework.spi.IGherkinExecutable;
import dev.galasa.framework.spi.IStatementOwner;
import dev.galasa.framework.spi.language.gherkin.ExecutionMethod;
import dev.galasa.framework.spi.language.gherkin.GherkinKeyword;
import dev.galasa.zos3270.TerminalInterruptedException;
import dev.galasa.zos3270.TextNotFoundException;
import dev.galasa.zos3270.Zos3270Exception;
import dev.galasa.zos3270.Zos3270ManagerException;
import dev.galasa.zos3270.internal.Zos3270ManagerImpl;
import dev.galasa.zos3270.spi.Zos3270TerminalImpl;

public class Gherkin3270CheckAppearsOnce  implements IStatementOwner {

    private final Gherkin3270Coordinator gerkinCoordinator;

    public Gherkin3270CheckAppearsOnce(Gherkin3270Coordinator gerkinCoordinator, Zos3270ManagerImpl manager) {
        this.gerkinCoordinator = gerkinCoordinator;
    }

    @ExecutionMethod(keyword = GherkinKeyword.THEN, regex = "check \"(.*)\" appears only once on terminal( \\w+)?")
    public void allocateTerminal(IGherkinExecutable executable, Map<String,Object> testVariables) throws Zos3270ManagerException, Zos3270Exception, TextNotFoundException, TerminalInterruptedException {
        List<String> groups = executable.getRegexGroups();  

        String terminalId = Gherkin3270Coordinator.defaultTerminaId(groups.get(1));
        String text = groups.get(0);
        if (text.isEmpty()) {
            return;
        }

        Zos3270TerminalImpl terminal = this.gerkinCoordinator.getTerminal(terminalId);
        if (!terminal.isConnected()) {
            throw new Zos3270ManagerException("Terminal '" + terminalId + "' is not connected");
        }

        String screen = terminal.retrieveScreen();
        
        int lastIndex = 0;
        int count = 0;

        while(lastIndex != -1){
            lastIndex = screen.indexOf(text, lastIndex);
            if(lastIndex != -1){
                count++;
                lastIndex += text.length();
            }
        }
        
        if (count == 0) {
            throw new Zos3270Exception("Text '" + text + "' is missing from terminal screen");
        }

        if (count > 1) {
            throw new Zos3270Exception("Text '" + text + "' appears multiple times on terminal screen");
        }

    }

}

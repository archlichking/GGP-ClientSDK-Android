
package com.openfeint.qa.core.caze.step;

import com.openfeint.qa.core.Scanner;
import com.openfeint.qa.core.command.After;
import com.openfeint.qa.core.command.And;
import com.openfeint.qa.core.command.Before;
import com.openfeint.qa.core.command.Cmd;
import com.openfeint.qa.core.command.Given;
import com.openfeint.qa.core.command.Then;
import com.openfeint.qa.core.command.When;
import com.openfeint.qa.core.exception.NoSuchStepException;
import com.openfeint.qa.core.util.CommandUtil;
import com.openfeint.qa.core.util.PackageUtil;
import com.openfeint.qa.core.util.StringUtil;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/***
 * @author thunderzhulei
 * @category build step by using text step defined in feature and java methods
 *           defined in StepDefinitions.java
 */
public class StepParser {
    public static final String STEP_SINGLE = "step_def";

    public static final String STEP_PATH = "step_path";

    private static StepHolder holder = null;

    public StepParser(String packageName, String apkPath) {
        // use single by default
        if (holder == null) {
            holder = new StepHolder(packageName, apkPath);
        }
    }

    public Step parse(String step) throws NoSuchStepException {
        String command = StringUtil.getFirstWord(step);
        String inst = StringUtil.getRestWord(step, command);
        Step s = new Step();
        switch (CommandUtil.Command.toCommand(command.toUpperCase())) {
            case GIVEN:
                // match given command
            case WHEN:
                // match when command
            case THEN:
                // match then command
            case AND:
                // match and command
            case BEFORE:
                // match before command
            case COMMAND:
                // match all-command command for all
            case AFTER:
                // match after command
                s.setCommand(step);
                holder.fixStep(inst, s, CommandUtil.CMD_FILTER);
                break;
            default:
                break;
        }
        return s;
    }

    private class StepHolder {
        private ArrayList<StepPair> aimStepList = new ArrayList<StepPair>();

        private StepHolder(String packageName, String apkPath) {
            try {
                List<Method> mList = PackageUtil.getAllStepMethods(packageName, apkPath);
                for (Method m : mList) {
                    if (null != m.getAnnotation(Given.class)) {
                        Pattern p = Pattern.compile(CommandUtil.CMD_FILTER + " "
                                + m.getAnnotation(Given.class).value());
                        aimStepList.add(new StepPair(p, m));
                        // continue;
                    }
                    if (null != m.getAnnotation(Then.class)) {
                        Pattern p = Pattern.compile(CommandUtil.CMD_FILTER + " "
                                + m.getAnnotation(Then.class).value());
                        aimStepList.add(new StepPair(p, m));
                        // continue;
                    }
                    if (null != m.getAnnotation(When.class)) {
                        Pattern p = Pattern.compile(CommandUtil.CMD_FILTER + " "
                                + m.getAnnotation(When.class).value());
                        aimStepList.add(new StepPair(p, m));
                        // continue;
                    }
                    if (null != m.getAnnotation(And.class)) {
                        Pattern p = Pattern.compile(CommandUtil.CMD_FILTER + " "
                                + m.getAnnotation(And.class).value());
                        aimStepList.add(new StepPair(p, m));
                        // continue;
                    }
                    if (null != m.getAnnotation(Before.class)) {
                        Pattern p = Pattern.compile(CommandUtil.CMD_FILTER + " "
                                + m.getAnnotation(Before.class).value());
                        aimStepList.add(new StepPair(p, m));
                        // continue;
                    }
                    if (null != m.getAnnotation(After.class)) {
                        Pattern p = Pattern.compile(CommandUtil.CMD_FILTER + " "
                                + m.getAnnotation(After.class).value());
                        aimStepList.add(new StepPair(p, m));
                        // continue;
                    }
                    if (null != m.getAnnotation(Cmd.class)) {
                        Pattern p = Pattern.compile(CommandUtil.CMD_FILTER + " "
                                + m.getAnnotation(Cmd.class).value());
                        aimStepList.add(new StepPair(p, m));
                        // continue;
                    }
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        /***
         * @param inst would be instruction behind command, such like I click on
         *            xxx button"
         * @param step aim step to insert into
         * @param command Given || When || Then || And || After || Before
         * @throws NoSuchStepException
         */
        public void fixStep(String inst, final Step step, String command)
                throws NoSuchStepException {
            // "inst" would be instruction behind command, such like
            // "I click on xxx button"

            // 1. get matched method in aimStepList
            StepPair sp = searchStep(inst, command);
            // 2. extract method and set in step
            step.setRef_method(Scanner.scanMethod(sp.getM()));
            // 3. extract parameter type and set in step
            step.setRef_method_param_types(Scanner.scanParameterTypes(sp.getM()));
            // 4. extract parameter value and set in step
            step.setRef_method_params(Scanner.scanParameterValues(sp, command + " " + inst));
            // 5. extract class for speed purpose
            step.setRef_class(Scanner.scanClass(sp.getM()));

            step.setKeyword(command);
        }

        private StepPair searchStep(String inst, String command) throws NoSuchStepException {
            for (StepPair sp : aimStepList) {
                if (sp.getP().matcher(command + " " + inst).matches()) {
                    return sp;
                }
            }
            throw new NoSuchStepException("No Such step [" + command + " " + inst + "]");
        }
    }

}

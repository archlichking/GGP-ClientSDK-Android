
package com.openfeint.qa.core.caze.step;

import com.openfeint.qa.core.caze.TestCase;
import com.openfeint.qa.core.util.StringUtil;
import com.openfeint.qa.core.util.type.TypeConversionException;
import com.openfeint.qa.core.util.type.TypeConverter;

import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;

/***
 * @author thunderzhulei
 * @category reflect a single text step defined in feature to a real java method
 *           in StepDefinition.java. including execute current step itself.
 */
public class Step {
    private String command;

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    private Class<?> ref_class;

    private Method ref_method;

    private Type[] ref_method_param_types;

    public Class<?> getRef_class() {
        return ref_class;
    }

    public void setRef_class(Class<?> refClass) {
        ref_class = refClass;
    }

    public Method getRef_method() {
        return ref_method;
    }

    public void setRef_method(Method refMethod) {
        ref_method = refMethod;
    }

    public Type[] getRef_method_param_types() {
        return ref_method_param_types;
    }

    public void setRef_method_param_types(Type[] refMethodParamTypes) {
        ref_method_param_types = refMethodParamTypes;
    }

    public Object[] getRef_method_params() {
        return ref_method_params;
    }

    public void setRef_method_params(Object[] refMethodParams) {
        ref_method_params = refMethodParams;
    }

    private Object[] ref_method_params;

    @SuppressWarnings("finally")
    public StepResult invoke() {
        int res = TestCase.RESULT.FAILED;
        String comm = "";
        try {
            Log.v(StringUtil.DEBUG_TAG,
                    "invoking [" + command + "] with step [" + ref_class.getName() + "."
                            + ref_method.getName() + "]");
            this.ref_method.invoke(this.getRef_class().newInstance(), this.buildRef_Params());
            res = TestCase.RESULT.PASSED;
        } catch (IllegalArgumentException e) {
            Log.e(StringUtil.DEBUG_TAG, e.getCause().getMessage());
        } catch (IllegalAccessException ek) {
            Log.e(StringUtil.DEBUG_TAG, ek.getCause().getMessage());
        } catch (InvocationTargetException er) {
            // most case failed reason raised here
            comm = command + " ==> " + er.getCause().getMessage();
            // for NullPointointException use
            er.printStackTrace();
            Log.e(StringUtil.DEBUG_TAG, comm);

        } finally {
            return new StepResult(res, comm);
        }
    }

    private Object[] buildRef_Params() throws TypeConversionException {
        ArrayList<Object> raw_params = new ArrayList<Object>();
        for (int i = 0; i < this.getRef_method_param_types().length; i++) {
            raw_params.add(TypeConverter.convertSimpleType(this.getRef_method_params()[i],
                    this.getRef_method_param_types()[i]));
        }
        return raw_params.toArray();
    }
}

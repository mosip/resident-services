package io.mosip.resident.util;

import org.mvel2.MVEL;
import org.mvel2.integration.VariableResolverFactory;
import org.mvel2.integration.impl.MapVariableResolverFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import static io.mosip.resident.constant.ResidentConstants.VALUE;

/**
 * @author Kamesh Shekhar Prasad
 */

@Component
public class MaskDataUtility {

    @Value("${resident.data.mask.function}")
    private String maskingFunction;

    @Value("${resident.email.mask.function}")
    private String emailMaskFunction;

    @Value("${resident.phone.mask.function}")
    private String phoneMaskFunction;

    public String maskEmail(String email) {
        return maskData(email, emailMaskFunction);
    }

    public String maskPhone(String phone) {
		return maskData(phone, phoneMaskFunction);
	}

    public String convertToMaskData(String maskData) {
        return maskData(maskData, maskingFunction);
    }

    @Autowired(required = true)
    @Qualifier("varres")
    private VariableResolverFactory functionFactory;

    public String maskData(Object object, String maskingFunctionName) {
        Map context = new HashMap();
        context.put(VALUE, String.valueOf(object));
        VariableResolverFactory myVarFactory = new MapVariableResolverFactory(context);
        myVarFactory.setNextFactory(functionFactory);
        Serializable serializable = MVEL.compileExpression(maskingFunctionName + "(value);");
        String formattedObject = MVEL.executeExpression(serializable, context, myVarFactory, String.class);
        return formattedObject;
    }
}

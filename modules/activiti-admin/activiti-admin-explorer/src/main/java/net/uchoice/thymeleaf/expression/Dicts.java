package net.uchoice.thymeleaf.expression;

import java.util.List;

import net.uchoice.sys.entity.Dict;
import net.uchoice.sys.utils.DictUtils;

public final class Dicts {

	public String getDictLabel(String value, String type,
			String defaultValue) {
		return DictUtils.getDictLabel(value, type, defaultValue);
	}

	public String getDictLabels(String values, String type,
			String defaultValue) {
		return DictUtils.getDictLabels(values, type, defaultValue);
	}

	public String getDictValue(String label, String type,
			String defaultLabel) {
		return DictUtils.getDictValue(label, type, defaultLabel);
	}

	public List<Dict> getDictList(String type) {
		return DictUtils.getDictList(type);
	}

	/**
	 * 返回字典列表（JSON）
	 * 
	 * @param type
	 * @return
	 */
	public String getDictListJson(String type) {
		return DictUtils.getDictListJson(type);
	}

}

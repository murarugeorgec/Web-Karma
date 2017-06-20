package edu.isi.karma.controller.update;

import java.io.PrintWriter;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import edu.isi.karma.rep.CellValue;
import edu.isi.karma.rep.Node;
import edu.isi.karma.util.JSONUtil;
import edu.isi.karma.view.VWorkspace;
import edu.isi.karma.view.ViewPreferences.ViewPreference;

/**
 * Provides information about a value update to a single node.
 * 
 * @author szekely
 * 
 */
public class GetSuggestionsUpdate extends AbstractUpdate {

	public enum JsonKeys {
		worksheet, nodeIds, displayNewValues, expandedNewValues, displayOldValues, expandedOldValues 
	}
	
	private final String worksheetId;

	private final List<String> nodeIds;

	private final List<String> suggestedValues;
	private final List<String> oldValues;
	
	public GetSuggestionsUpdate(String worksheetId, List<String> nodeIds,
			List<String> suggestedValues, List<String> oldValues) {
		super();
		this.worksheetId = worksheetId;
		this.nodeIds = nodeIds;
		this.suggestedValues = suggestedValues;
		this.oldValues = oldValues;
	}

	@Override
	public void generateJson(String prefix, PrintWriter pw, VWorkspace vWorkspace) {
		final List<String> expandedNewValues = new ArrayList<String>();
		final List<String> expandedOldValues = new ArrayList<String>();
		
		int maxValueLength = vWorkspace.getPreferences().getIntViewPreferenceValue(
				ViewPreference.maxCharactersInCell);

		for (String val : suggestedValues) {
			if (val.length() > maxValueLength) {
				expandedNewValues.add(JSONUtil.truncateCellValue(val, maxValueLength));
			} else {
				expandedNewValues.add(val);
			}
		}

		for (String val : oldValues) {
			if (val.length() > maxValueLength) {
				expandedOldValues.add(JSONUtil.truncateCellValue(val, maxValueLength));
			} else {
				expandedOldValues.add(val);
			}
		}


		pw.println(prefix + "{");
		String newPref = prefix + "  ";
		pw.println(newPref + JSONUtil.json(GenericJsonKeys.updateType, getUpdateType()));
		pw.println(newPref + JSONUtil.json(JsonKeys.worksheet, worksheetId));

		addListValues(nodeIds, JsonKeys.nodeIds, pw, newPref, false);
		addListValues(expandedNewValues, JsonKeys.displayNewValues, pw, newPref, false);
		addListValues(suggestedValues, JsonKeys.expandedNewValues, pw, newPref, false);
		addListValues(expandedOldValues, JsonKeys.displayOldValues, pw, newPref, false);

		addListValues(oldValues, JsonKeys.expandedOldValues, pw, newPref, true);

		pw.println(prefix + "}");
	}

	private void addListValues(List<String> values, JsonKeys jsonKey, PrintWriter pw, String pref, boolean isFinal) {

		pw.println(pref + JSONUtil.jsonStartList(jsonKey));
		Iterator<String> it = values.iterator();

		while (it.hasNext()) {
			String val = it.next();
			pw.println(pref + JSONUtil.doubleQuote(val));
			if (it.hasNext()) {
				pw.println(pref + " ,");
			}

		}

		pw.print(pref + "]");

		if (!isFinal) {
			pw.println(",");
		} else {
			pw.println("");
		}
	}
	
	public boolean equals(Object o) {
		if (o instanceof GetSuggestionsUpdate) {
			GetSuggestionsUpdate t = (GetSuggestionsUpdate) o;
			return t.worksheetId.equals(worksheetId) && t.nodeIds.equals(nodeIds);
		}

		return false;
	}
}

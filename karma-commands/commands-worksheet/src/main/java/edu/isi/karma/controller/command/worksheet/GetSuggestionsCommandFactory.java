package edu.isi.karma.controller.command.worksheet;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONException;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.JSONInputCommandFactory;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.util.CommandInputJSONUtil;
import edu.isi.karma.webserver.KarmaException;

public class GetSuggestionsCommandFactory extends JSONInputCommandFactory {

	public enum Arguments {
		worksheetId, hTableId, hNodeId, distanceMetric, threshold, selectionName
	}
	
	@Override
	public Command createCommand(HttpServletRequest request,
			Workspace workspace) {
		String hNodeId = request.getParameter(Arguments.hNodeId.name());
		String hTableId = request.getParameter(Arguments.hTableId.name());
		String worksheetId = request.getParameter(Arguments.worksheetId.name());
		String distanceMetric = request.getParameter(Arguments.distanceMetric.name());
		Double threshold = Double.parseDouble(request.getParameter(Arguments.threshold.name()));
		String selectionName = request.getParameter(Arguments.selectionName.name());
		return new GetSuggestionsCommand(getNewId(workspace), Command.NEW_MODEL, worksheetId, 
				hTableId, hNodeId, distanceMetric, threshold, selectionName);
	}

	@Override
	public Command createCommand(JSONArray inputJson, String model, Workspace workspace)
			throws JSONException, KarmaException {
		/** Parse the input arguments and create proper data structures to be passed to the command **/
		String hNodeID = CommandInputJSONUtil.getStringValue(Arguments.hNodeId.name(), inputJson);
		String worksheetId = CommandInputJSONUtil.getStringValue(Arguments.worksheetId.name(), inputJson);
		String hTableId = CommandInputJSONUtil.getStringValue(Arguments.hTableId.name(), inputJson);
		String distanceMetric = CommandInputJSONUtil.getStringValue(Arguments.distanceMetric.name(), inputJson);
		Double threshold = Double.parseDouble(CommandInputJSONUtil.getStringValue(Arguments.threshold.name(), inputJson));

		this.normalizeSelectionId(worksheetId, inputJson, workspace);
                String selectionName = CommandInputJSONUtil.getStringValue(Arguments.selectionName.name(), inputJson);
		GetSuggestionsCommand colCmd = new GetSuggestionsCommand(getNewId(workspace), model, worksheetId,
				hTableId, hNodeID, distanceMetric, threshold, selectionName);
		colCmd.setInputParameterJson(inputJson.toString());
		return colCmd;
	}

	@Override
	public Class<? extends Command> getCorrespondingCommand() {
		return GetSuggestionsCommand.class;
	}
}

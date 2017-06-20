package edu.isi.karma.controller.command.worksheet;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONException;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.JSONInputCommandFactory;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.util.CommandInputJSONUtil;
import edu.isi.karma.webserver.KarmaException;

public class SetSuggestionsCommandFactory extends JSONInputCommandFactory {

	public enum Arguments {
		worksheetId, hTableId, hNodeId, setSuggestedNodes, selectionName
	}
	
	@Override
	public Command createCommand(HttpServletRequest request,
			Workspace workspace) {
		
		String hNodeId = request.getParameter(Arguments.hNodeId.name());
		String hTableId = request.getParameter(Arguments.hTableId.name());
		String worksheetId = request.getParameter(Arguments.worksheetId.name());
		String selectionName = request.getParameter(Arguments.selectionName.name());
		String setSuggestedNodes = request.getParameter(Arguments.setSuggestedNodes.name());
		return new SetSuggestionsCommand(getNewId(workspace), Command.NEW_MODEL,
				worksheetId, hTableId, hNodeId, setSuggestedNodes, selectionName);
	}

	@Override
	public Command createCommand(JSONArray inputJson, String model, Workspace workspace)
			throws JSONException, KarmaException {
		/** Parse the input arguments and create proper data structures to be passed to the command **/
		String hNodeId = CommandInputJSONUtil.getStringValue(Arguments.hNodeId.name(), inputJson);
		String hTableId = CommandInputJSONUtil.getStringValue(Arguments.hTableId.name(), inputJson);
		String worksheetId = CommandInputJSONUtil.getStringValue(Arguments.worksheetId.name(), inputJson);

		this.normalizeSelectionId(worksheetId, inputJson, workspace);
                String selectionName = CommandInputJSONUtil.getStringValue(Arguments.selectionName.name(), inputJson);
		String setSuggestedNodes = CommandInputJSONUtil.getStringValue(Arguments.setSuggestedNodes.name(), inputJson);
		SetSuggestionsCommand colCmd = new SetSuggestionsCommand(getNewId(workspace), model,
							worksheetId, hTableId, hNodeId, setSuggestedNodes, selectionName);
		colCmd.setInputParameterJson(inputJson.toString());
		return colCmd;
	}

	@Override
	public Class<? extends Command> getCorrespondingCommand() {
		return SetSuggestionsCommand.class;
	}
}

package edu.isi.karma.controller.command.worksheet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.command.ICommand;
import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.CommandType;
import edu.isi.karma.controller.command.WorksheetSelectionCommand;
import edu.isi.karma.controller.command.selection.SuperSelection;
import edu.isi.karma.controller.command.worksheet.EditCellCommand;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.controller.update.InfoUpdate;
import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.GetSuggestionsUpdate;
import edu.isi.karma.controller.update.WorksheetUpdateFactory;

import edu.isi.karma.rep.HNode;
import edu.isi.karma.rep.HNode.HNodeType;
import edu.isi.karma.rep.HNodePath;
import edu.isi.karma.rep.HTable;
import edu.isi.karma.rep.Node;
import edu.isi.karma.rep.CellValue;
import edu.isi.karma.rep.Node.NodeStatus;
import edu.isi.karma.rep.RepFactory;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.util.Util;

import java.util.Arrays;

import java.io.*;
import java.util.HashMap;
import java.util.Map;


/**
 * Adds a new column to the table with hTableId.
 * hTableId may be empty if a hNodeId is provided.
 * If hNodeId is provided, adds the new column after hNodeId.
 * If no hNodeId is provided adds the new column as the first column in the table hTableId.
 * Returns the hNodeId of the newly created column.
 */
public class SetSuggestionsCommand extends WorksheetSelectionCommand {
	// add column to this table
	private String hTableId;

	// if null add column at beginning of table
        private final String hNodeId;

	private List<ICommand> editedHNodeIds;
	private String textExtract;

	private static Logger logger = LoggerFactory.getLogger(GetSuggestionsCommand.class);

	protected SetSuggestionsCommand(String id, String model, String worksheetId, String hTableId, String hNodeId,
					String textExtract, String selectionId) {
		super(id, model, worksheetId, selectionId);
		this.hTableId = hTableId;
		this.hNodeId = hNodeId;
		this.textExtract = textExtract;
		editedHNodeIds = new ArrayList<ICommand>();

		addTag(CommandTag.Transformation);
	}

	@Override
	public String getCommandName() {
		return SetSuggestionsCommand.class.getSimpleName();
	}

	@Override
	public String getTitle() {
		return "Set Suggestions";
	}

	@Override
	public String getDescription() {
		return "add suggestions";
	}

	@Override
	public CommandType getCommandType() {
		return CommandType.undoable;
	}

	@Override
	public UpdateContainer doIt(Workspace workspace) {
		logger.error(this.textExtract);
		inputColumns.clear();
		outputColumns.clear();

		inputColumns.add(hNodeId);
		outputColumns.add(hNodeId);

		UpdateContainer uc = new UpdateContainer(new InfoUpdate("Set Suggestions"));

		try {
			populateColumnWithSuggestedValue();
		} catch (CommandException e) {
			logger.error("Error in SetSuggestionsCommand - populateColumnWithSuggestedValue" + e.toString());
			Util.logException(logger, e);
			return new UpdateContainer(new ErrorUpdate(e.getMessage()));
		}

		for (ICommand comm: editedHNodeIds) {
			try {
				uc.append(comm.doIt(workspace));
			} catch (CommandException e) {
				logger.error("Error in GetSuggestionsCommand" + e.toString());
				Util.logException(logger, e);
				return new UpdateContainer(new ErrorUpdate(e.getMessage()));
			}
		}

		return uc;
	}

	private void populateColumnWithSuggestedValue() throws CommandException {
		//HNodePath selectedPath = null;
		Map<String, String> newNodes = new HashMap<>();
                String[] nodesChange = textExtract.split("-;-");

		for (int i = 0; i < nodesChange.length; i += 3) {
			String[] values = nodesChange[i].split(";-;");

			newNodes.put(values[1], values[0]);
		}

		for (Map.Entry<String, String> entry : newNodes.entrySet()) {
			editedHNodeIds.add(new EditCellCommand(id, model, worksheetId,
						entry.getKey(), entry.getValue(), selectionId));
		}

	}

	@Override
	public UpdateContainer undoIt(Workspace workspace) {
		Worksheet worksheet = workspace.getWorksheet(worksheetId);
		SuperSelection selection = getSuperSelection(workspace);

		UpdateContainer uc = WorksheetUpdateFactory.createWorksheetHierarchicalAndCleaningResultsUpdates(worksheetId,
								selection, workspace.getContextId());
		for (ICommand comm: editedHNodeIds) {
			uc.append(comm.undoIt(workspace));
		}

		return uc;
	}
}

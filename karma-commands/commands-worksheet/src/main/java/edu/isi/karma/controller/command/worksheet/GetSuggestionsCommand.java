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

import java.util.Set;
import java.util.HashSet;
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
public class GetSuggestionsCommand extends WorksheetSelectionCommand {
	// add column to this table
	private String hTableId;

	// if null add column at beginning of table
        private final String hNodeId;

	// To know the type of distance
	private final String distance;

	private final Double threshold;

	private List<ICommand> editedHNodeIds;
	List<String> nodeIds;
	List<String> newValues, oldValues;

	private static Logger logger = LoggerFactory.getLogger(GetSuggestionsCommand.class);

	protected GetSuggestionsCommand(String id, String model, String worksheetId,
			String hTableId, String hNodeId, String distanceMetric,
			Double threshold, String selectionId) {
		super(id, model, worksheetId, selectionId);
		this.hNodeId = hNodeId;
		this.hTableId = hTableId;
		this.distance = distanceMetric;
		this.threshold = threshold;
		this.nodeIds = new ArrayList<String>();
		this.newValues = new ArrayList<String>();
		this.oldValues = new ArrayList<String>();
		editedHNodeIds = new ArrayList<ICommand>();

		addTag(CommandTag.Transformation);
	}

	@Override
	public String getCommandName() {
		return GetSuggestionsCommand.class.getSimpleName();
	}

	@Override
	public String getTitle() {
		return "Get Suggestions";
	}

	@Override
	public String getDescription() {
		return this.distance;
	}

	@Override
	public CommandType getCommandType() {
		return CommandType.undoable;
	}

	@Override
	public UpdateContainer doIt(Workspace workspace) {
		inputColumns.clear();
		outputColumns.clear();

		inputColumns.add(hNodeId);
		outputColumns.add(hNodeId);

		Worksheet worksheet = workspace.getWorksheet(worksheetId);
		Map<String, String> suggestions = null;

		try {
			suggest(worksheet, workspace.getFactory());
		} catch (CommandException e) {
			logger.error("Error in GetSuggestionsCommand - similarity" + e.toString());
			Util.logException(logger, e);
			return new UpdateContainer(new ErrorUpdate(e.getMessage()));
		}
	
		System.out.println(nodeIds);
		System.out.println(newValues);
		System.out.println(oldValues);
		UpdateContainer uc = new UpdateContainer(new GetSuggestionsUpdate(worksheetId, nodeIds, newValues, oldValues));

		return uc;
	}


	private void suggest(Worksheet worksheet, RepFactory factory) throws CommandException {
		final HashMap<String, Integer> occurrences = new HashMap<String, Integer>();
		SuperSelection selection = getSuperSelection(worksheet);
		List<HNodePath> columnPaths = worksheet.getHeaders().getAllPaths();
		HNodePath selectedPath = null;

		for (HNodePath path : columnPaths) {
			if (path.getLeaf().getId().equals(hNodeId)) {
				selectedPath = path;
			}
		}

		Collection<Node> nodes = new ArrayList<>(Math.max(1000, worksheet.getDataTable().getNumRows()));
		worksheet.getDataTable().collectNodes(selectedPath, nodes, selection);

		for (Node node : nodes) {
			String nodeVal = node.getValue().asString();
			if (nodeVal.length() != 0) {
				Integer oldNr = occurrences.get(nodeVal);
				occurrences.put(nodeVal, oldNr == null ? 1 : oldNr + 1);
			}
		}

		final Map<String, String> maybeMistaken = getSuggestions(occurrences);

		for (Node node : nodes) {
			String nodeVal = node.getValue().asString();
			String val = maybeMistaken.get(nodeVal);
			if (val != null) {
				nodeIds.add(node.getId());
				oldValues.add(nodeVal);
				newValues.add(val);
			}
		}
	}


	/*
	@Override
	public UpdateContainer doIt(Workspace workspace) {
		inputColumns.clear();
		outputColumns.clear();
		Worksheet worksheet = workspace.getWorksheet(worksheetId);

		inputColumns.add(hNodeId);
		outputColumns.add(hNodeId);

		UpdateContainer uc = new UpdateContainer(new InfoUpdate("Get Suggestions"));
		try {
			populateColumnWithSuggestedValue(worksheet, workspace.getFactory());
		} catch (CommandException e) {
			logger.error("Error in GetSuggestionsCommand - similarity" + e.toString());
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

	private void populateColumnWithSuggestedValue(Worksheet worksheet, RepFactory factory) throws CommandException {
		final HashMap<String, Integer> occurrences = new HashMap<String, Integer>();
		SuperSelection selection = getSuperSelection(worksheet);
		List<HNodePath> columnPaths = worksheet.getHeaders().getAllPaths();
		HNodePath selectedPath = null;

		for (HNodePath path : columnPaths) {
			if (path.getLeaf().getId().equals(hNodeId)) {
				selectedPath = path;
			}
		}

		Collection<Node> nodes = new ArrayList<>(Math.max(1000, worksheet.getDataTable().getNumRows()));
		worksheet.getDataTable().collectNodes(selectedPath, nodes, selection);

		for (Node node : nodes) {
			String nodeVal = node.getValue().asString();
			if (nodeVal.length() != 0) {
				Integer oldNr = occurrences.get(nodeVal);
				occurrences.put(nodeVal, oldNr == null ? 1 : oldNr + 1);
			}
		}

		final Map<String, String> maybeMistaken = getSuggestions(occurrences);

		for (Node node : nodes) {
			String nodeVal = node.getValue().asString();
			if (maybeMistaken.containsKey(nodeVal)) {
				editedHNodeIds.add(new EditCellCommand(id, model, worksheetId,
							node.getId(), maybeMistaken.get(nodeVal), selectionId));
			}
		}
	}
	*/

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

	private Map<String, String> getSuggestions(final Map<String, Integer> occurrences) throws CommandException {
		final Set<String> maybeMistake = new HashSet<String>();
		final Map<String, String> maybeMap = new HashMap<String, String>();

		/* Compute the threshold taking into consideration the total number of unique values */
		final Double threshold_occ = threshold / occurrences.size();

		Integer max = Integer.MAX_VALUE;
		Integer min = Integer.MIN_VALUE;


		for (Map.Entry<String, Integer> entry: occurrences.entrySet()) {
			if (entry.getValue() > max) {
				max = entry.getValue();
			}

			if (entry.getValue() < min) {
				min = entry.getValue();
			}

			if (((float) entry.getValue()) / occurrences.size() < threshold_occ) {
				maybeMistake.add(entry.getKey());
			}
		}

		for (String s: maybeMistake) {
			System.out.println(s);
			System.out.println(occurrences.get(s));
		}

		for (String s1: maybeMistake) {
			Double minimum_sim = Double.MAX_VALUE;
			for (Map.Entry<String, Integer> entry: occurrences.entrySet()) {
				final String s2 = entry.getKey();
				if (maybeMistake.contains(s2)) {
					continue;
				}

				Double sim;

				if (distance.equals("jaro-winkler")) {
					sim = similarity_jw(s1, s2);
				} else if (distance.equals("levenshtein")) {
					sim = similary_lev(s1, s2);
				} else {
					throw new CommandException(this, "Distance metric not known " + distance);
				}

				if (minimum_sim > sim) {
					minimum_sim = sim;
					maybeMap.put(s1, s2);
				}
			}

			System.out.println(s1);
			System.out.println(maybeMap.get(s1));
		}

		return maybeMap;
	}


	private Integer getPrefix(String s1, String s2) {
		final Integer n1 = s1.length();
		final Integer n2 = s2.length();

		Integer prefix = 0;
		for (int i = 0; i < Integer.min(n1, n2); i++) {
			if (s1.charAt(i) != s2.charAt(i)) {
				break;
			}

			prefix++;
		}

		return prefix;
	}


	private Double similarity_jw(String s1, String s2) {
		final Double threshold_jw = 0.1;
		final Integer n1 = s1.length();
		final Integer n2 = s2.length();

		final Integer maxMatch = Integer.max(n1, n2) / 2 - 1;
		final Boolean[] s1_match = new Boolean[n1];
		Arrays.fill(s1_match, Boolean.FALSE);

		final Boolean[] s2_match = new Boolean[n2];
		Arrays.fill(s2_match, Boolean.FALSE);

		Integer nr_match = 0;
		Integer trans = 0;

		for (int i = 0; i < n1; i++) {
		    final Integer start = Integer.max(0, i - maxMatch);
		    final Integer end = Integer.min(i + maxMatch, n2);

		    for (int j = start; j < end; j++) {
			if (s2_match[j]) continue;
			if (s1.charAt(i) != s2.charAt(j)) continue;
			s1_match[i] = true;
			s2_match[j] = true;
			nr_match++;
			break;
		    }
		}

		for (int i = 0, k = 0; i < n1; i++, k++) {
		    if (!s1_match[i]) continue;
		    while (k < n2 && !s2_match[k]) k++;
		    if (k < n2 && s1.charAt(i) != s2.charAt(k)) trans++;
		}

		final Double jaro = (((double) nr_match / n1) +
			((double) nr_match / n2) +
			(((double) nr_match - trans / 2.0) / nr_match)) / 3.0;

		final Integer prefix = getPrefix(s1, s2);
		final Double jaro_winkler = jaro + prefix * threshold_jw * (1 - jaro);

		return 1.0 - jaro_winkler;
	}


	private Double similary_lev(String s1, String s2) {
		int[][] dist = new int[s1.length() + 1][s2.length() + 1];

		for (int i = 0; i <= s1.length(); i++)
			dist[i][0] = i;
		for (int i = 1; i <= s2.length(); i++)
			dist[0][i] = i;

		for (int i = 1; i <= s1.length(); i++) {
			for (int j = 1; j <= s2.length(); j++) {
				Integer cost = 0;
				if (s1.charAt(i-1) != s2.charAt(j-1)) {
					cost = 1;
				}

				dist[i][j] = Math.min(dist[i - 1][j] + 1, Math.min(dist[i][j - 1] + 1, dist[i - 1][j - 1] + cost));
			}
		}

		return (double) dist[s1.length()][s2.length()];

	}
}

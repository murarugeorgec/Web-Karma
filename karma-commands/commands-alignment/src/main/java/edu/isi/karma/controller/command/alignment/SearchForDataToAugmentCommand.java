package edu.isi.karma.controller.command.alignment;

import java.util.HashMap;
import java.util.List;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.CommandType;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.er.helper.TripleStoreUtil;
import edu.isi.karma.modeling.alignment.Alignment;
import edu.isi.karma.modeling.alignment.AlignmentManager;
import edu.isi.karma.rep.alignment.Node;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.webserver.KarmaException;

public class SearchForDataToAugmentCommand extends Command{
	private String tripleStoreUrl;
	private String context;
	private String nodeType;
	private String alignmentId;
	public SearchForDataToAugmentCommand(String id, String url, String context, String nodeType, String alignmentId) {
		super(id);
		this.tripleStoreUrl = url;
		this.context = context;
		this.nodeType = nodeType;
		this.alignmentId = alignmentId;
	}

	@Override
	public String getCommandName() {
		// TODO Auto-generated method stub
		return this.getClass().getSimpleName();
	}

	@Override
	public String getTitle() {
		// TODO Auto-generated method stub
		return "Search For Data To Augment";
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CommandType getCommandType() {
		// TODO Auto-generated method stub
		return CommandType.notInHistory;
	}

	@Override
	public UpdateContainer doIt(Workspace workspace) throws CommandException {
		// TODO Auto-generated method stub
		AlignmentManager manager = new AlignmentManager();
		Alignment alignment = manager.getAlignment(alignmentId);
		Node node = alignment.GetTreeRoot();
		nodeType = node.getLabel().getPrefix();
		UpdateContainer uc = new UpdateContainer();
		TripleStoreUtil util = new TripleStoreUtil();
		HashMap<String, List<String>> result = null;
		try {
			result = util.getPredicatesForTriplesMapsWithSameClass(tripleStoreUrl, context, nodeType);
		} catch (KarmaException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return uc;
	}

	@Override
	public UpdateContainer undoIt(Workspace workspace) {
		// TODO Auto-generated method stub
		return null;
	}

}
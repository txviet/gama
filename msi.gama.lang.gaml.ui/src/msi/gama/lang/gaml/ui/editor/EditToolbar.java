package msi.gama.lang.gaml.ui.editor;

import java.util.ArrayList;
import msi.gama.common.*;
import msi.gama.common.GamaPreferences.IPreferenceChangeListener;
import msi.gama.gui.swt.*;
import msi.gama.gui.swt.commands.GamaColorMenu;
import msi.gama.gui.swt.controls.GamaToolbar;
import msi.gaml.compilation.GamlCompilationError;
import msi.gaml.descriptions.ModelDescription;
import msi.gaml.factories.DescriptionFactory;
import org.eclipse.core.commands.*;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.texteditor.*;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.util.concurrent.IUnitOfWork;

/**
 * This class implements the GAML editors' toolbar
 * 
 * @author Alexis Drogoul, dec 2014
 * 
 */
// @SuppressWarnings("restriction")
public class EditToolbar {

	private GridData editGridData;
	private GamaToolbar toolbar;
	private ToolItem minus, mark, line, folding;
	private final GamlEditor editor;
	private Font font;
	private static boolean listenersRegistered = false;

	static {
		// Fake operations to force the preferences to load and show
		GamaColorMenu.COLOR_MENU_SORT.getKey();
		GamaColorMenu.COLOR_MENU_REVERSE.getKey();
		EditToolbarOperatorsMenu.OPERATORS_MENU_SORT.getKey();
	}

	public static interface IToolbarVisitor {

		void visit(EditToolbar toolbar);
	}

	public static void visitToolbars(final IToolbarVisitor visitor) {
		IEditorReference[] eds = SwtGui.getPage().getEditorReferences();
		for ( IEditorReference ed : eds ) {
			IEditorPart e = ed.getEditor(false);
			if ( e instanceof GamlEditor ) {
				visitor.visit(((GamlEditor) e).getEditToolbar());
			}
		}
	}

	public EditToolbar(final GamlEditor editor, final Composite parent) {
		this.editor = editor;
		createToolbar(parent);
		registerListeners();
	}

	public void installGesturesFor(final GamlEditor editor) {
		StyledText text = editor.getInternalSourceViewer().getTextWidget();
		if ( text != null ) {
			text.addGestureListener(new GestureListener() {

				@Override
				public void gesture(final GestureEvent ge) {
					if ( ge.detail == SWT.GESTURE_BEGIN ) {

					} else

					if ( ge.detail == SWT.GESTURE_MAGNIFY ) {
						if ( ge.magnification > 1.0 ) {
							setFontAndCheckButtons(1);
						} else if ( ge.magnification < 1.0 ) {
							setFontAndCheckButtons(-1);
						}
					}
				}
			});
		}
	}

	public void setVisible(final boolean visible) {
		editGridData.exclude = !visible;
		toolbar.setVisible(visible);
		toolbar.getParent().getParent().layout();
		// toolbar.getParent().layout(true);
	}

	public void createToolbar(final Composite parentComposite) {
		toolbar =
			new GamaToolbar(parentComposite, SWT.FLAT | SWT.HORIZONTAL | SWT.WRAP | SWT.FILL | SWT.BORDER).color(
				IGamaColors.WHITE.color()).width(parentComposite);
		editGridData = new GridData(SWT.FILL, SWT.FILL, true, false);
		editGridData.verticalIndent = 0;
		editGridData.horizontalAlignment = SWT.LEFT;
		toolbar.setLayoutData(editGridData);

		// toolbar.label("Presentation");
		// toolbar.sep(16);
		line = toolbar.check("editor.line2", null, "Toogle the display of line numbers", new SelectionAdapter() {

			@Override
			public void widgetSelected(final SelectionEvent e) {
				editor.getAction(ITextEditorActionConstants.LINENUMBERS_TOGGLE).run();
			}
		});
		line.setSelection(editor.isLineNumberRulerVisible());
		folding = toolbar.check("editor.folding2", null, "Toogle the folding of lines", new SelectionAdapter() {

			@Override
			public void widgetSelected(final SelectionEvent e) {
				editor.getAction("FoldingToggle").run();
			}
		});
		folding.setSelection(editor.isRangeIndicatorEnabled());

		// Mark Occurences button (synchronized with the global preference)
		final GamaPreferences.Entry<Boolean> pref = GamaPreferences.get("editor.mark.occurences", Boolean.class);
		mark = toolbar.check("editor.mark2", null, "Mark occurences", new SelectionAdapter() {

			@Override
			public void widgetSelected(final SelectionEvent e) {
				pref.set(((ToolItem) e.widget).getSelection()).save();
			}
		});
		mark.setSelection(pref.getValue());

		// EditBox button
		toolbar.check("editor.editbox2", null, "Toggle colorization of code sections ", new SelectionAdapter() {

			@Override
			public void widgetSelected(final SelectionEvent e) {
				boolean selection = ((ToolItem) e.widget).getSelection();
				editor.setDecorationEnabled(selection);
				editor.decorate(selection);
			}

		}).setSelection(editor.isDecorationEnabled());
		minus = toolbar.button("editor.decrease2", "", "Decrease font size", new SelectionAdapter() {

			@Override
			public void widgetSelected(final SelectionEvent e) {
				setFontAndCheckButtons(-1);
			}
		});
		toolbar.button("editor.increase2", "", "Increase font size", new SelectionAdapter() {

			@Override
			public void widgetSelected(final SelectionEvent e) {
				setFontAndCheckButtons(1);
			}
		});
		toolbar.sep(12);
		// toolbar.label("Search");
		// toolbar.sep(16);

		new EditToolbarFindControls(editor).fill(toolbar);
		// toolbar.sep();
		// toolbar.label("Navigation");
		toolbar.sep(12);

		final ToolItem lastEdit = toolbar.button("editor.lastedit2", null, "Previous location", new SelectionAdapter() {

			@Override
			public void widgetSelected(final SelectionEvent e) {
				try {
					ICommandService service = (ICommandService) editor.getSite().getService(ICommandService.class);
					Command c = service.getCommand(IWorkbenchCommandConstants.NAVIGATE_BACKWARD_HISTORY);
					if ( c.isEnabled() ) {
						IHandlerService handlerService =
							(IHandlerService) editor.getSite().getService(IHandlerService.class);
						handlerService.executeCommand(IWorkbenchCommandConstants.NAVIGATE_BACKWARD_HISTORY, null);
					}
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});

		final ToolItem nextEdit = toolbar.button("editor.nextedit2", null, "Next location", new SelectionAdapter() {

			@Override
			public void widgetSelected(final SelectionEvent e) {
				try {
					ICommandService service = (ICommandService) editor.getSite().getService(ICommandService.class);
					Command c = service.getCommand(IWorkbenchCommandConstants.NAVIGATE_FORWARD_HISTORY);
					if ( c.isEnabled() ) {
						IHandlerService handlerService =
							(IHandlerService) editor.getSite().getService(IHandlerService.class);
						handlerService.executeCommand(IWorkbenchCommandConstants.NAVIGATE_FORWARD_HISTORY, null);
					}
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});

		// Attaching listeners to the global commands in order to enable/disable the toolbar items
		ICommandService service = (ICommandService) editor.getSite().getService(ICommandService.class);
		final Command nextCommand = service.getCommand(IWorkbenchCommandConstants.NAVIGATE_FORWARD_HISTORY);
		nextEdit.setEnabled(nextCommand.isEnabled());
		final ICommandListener nextListener = new ICommandListener() {

			@Override
			public void commandChanged(final CommandEvent e) {
				nextEdit.setEnabled(nextCommand.isEnabled());
			}
		};

		nextCommand.addCommandListener(nextListener);
		final Command lastCommand = service.getCommand(IWorkbenchCommandConstants.NAVIGATE_BACKWARD_HISTORY);
		final ICommandListener lastListener = new ICommandListener() {

			@Override
			public void commandChanged(final CommandEvent e) {
				lastEdit.setEnabled(lastCommand.isEnabled());
			}
		};
		lastEdit.setEnabled(lastCommand.isEnabled());
		lastCommand.addCommandListener(lastListener);
		// Attaching dispose listeners to the toolItems so that they remove the command listeners properly
		lastEdit.addDisposeListener(new DisposeListener() {

			@Override
			public void widgetDisposed(final DisposeEvent e) {
				lastCommand.removeCommandListener(lastListener);
			}
		});
		nextEdit.addDisposeListener(new DisposeListener() {

			@Override
			public void widgetDisposed(final DisposeEvent e) {
				nextCommand.removeCommandListener(nextListener);
			}
		});

		toolbar.menu("editor.outline2", null, "Show outline", new SelectionAdapter() {

			@Override
			public void widgetSelected(final SelectionEvent e) {
				EditToolbarOutlinePopup.open(editor, e);
			}
		});

		// Next: formatting commands
		toolbar.sep(12);
		// toolbar.label("Format");
		// toolbar.sep(16);

		toolbar.button("editor.leftshift2", null, "Shift left", new SelectionAdapter() {

			@Override
			public void widgetSelected(final SelectionEvent e) {
				editor.getAction(ITextEditorActionConstants.SHIFT_LEFT).run();
			}
		});
		toolbar.button("editor.rightshift2", null, "Shift right", new SelectionAdapter() {

			@Override
			public void widgetSelected(final SelectionEvent e) {
				editor.getAction(ITextEditorActionConstants.SHIFT_RIGHT).run();
			}
		});
		toolbar.button("editor.format2", null, "Format", new SelectionAdapter() {

			@Override
			public void widgetSelected(final SelectionEvent e) {
				editor.getAction("Format").run();
			}
		});
		toolbar.button("editor.serialize2", null, "Re-serialize the model (warning: removes all comments)",
			new SelectionAdapter() {

				@Override
				public void widgetSelected(final SelectionEvent e) {
					editor.getInternalSourceViewer().setSelectedRange(0,
						editor.getInternalSourceViewer().getTextWidget().getCharCount());
					String result = editor.getDocument().modify(new IUnitOfWork<String, XtextResource>() {

						@Override
						public String exec(final XtextResource state) throws Exception {
							if ( state.getErrors().isEmpty() ) {
								java.util.List<GamlCompilationError> list = new ArrayList();
								ModelDescription md =
									DescriptionFactory.getModelFactory().buildModelDescription(state.getURI(), list);
								if ( md != null ) {
									md = (ModelDescription) md.validate();
									if ( !state.getErrors().isEmpty() ) { return null; }
								}
								if ( md != null && list.isEmpty() ) { return md.serialize(false); }
							}
							return null;
						}
					});
					if ( result != null ) {
						editor.getInternalSourceViewer().setSelectedRange(0,
							editor.getInternalSourceViewer().getTextWidget().getCharCount());
						editor.insertText(result);
					}
				}
			});
		toolbar.button("editor.comment2", null, "Toggle comment", new SelectionAdapter() {

			@Override
			public void widgetSelected(final SelectionEvent e) {
				editor.getAction("ToggleComment").run();
			}
		});
		toolbar.button("editor.block2", null, "Toggle block comment", new SelectionAdapter() {

			@Override
			public void widgetSelected(final SelectionEvent e) {
				editor.toggleBlockComment();
			}
		});

		// Next: template commands
		// toolbar.sep();
		// toolbar.label("Templates");
		toolbar.sep(12);

		toolbar.menu("editor.templates2", null, "Templates", new SelectionAdapter() {

			@Override
			public void widgetSelected(final SelectionEvent e) {
				EditToolbarMenuFactory.getInstance().openTemplateMenu(editor, e);
			}
		});

		toolbar.menu("editor.builtin2", null, "Built-in attributes and actions", new SelectionAdapter() {

			@Override
			public void widgetSelected(final SelectionEvent e) {
				EditToolbarMenuFactory.getInstance().openBuiltInMenu(editor, e);
			}
		});

		toolbar.menu("editor.operators2", null, "Operators", new SelectionAdapter() {

			@Override
			public void widgetSelected(final SelectionEvent e) {
				EditToolbarMenuFactory.getInstance().openOperatorsMenu(editor, e);
			}
		});

		toolbar.menu("editor.color2", null, "Colors", new SelectionAdapter() {

			@Override
			public void widgetSelected(final SelectionEvent e) {
				EditToolbarMenuFactory.getInstance().openColorMenu(editor, e);
			}

		});

	}

	private void setFontAndCheckButtons(final int deltaToApply) {
		StyledText text = editor.getInternalSourceViewer().getTextWidget();
		FontData data = text.getFont().getFontData()[0];
		data.height += deltaToApply;
		if ( data.height < 6 ) { return; }
		if ( font != null ) {
			if ( data.equals(font.getFontData()[0]) ) { return; }
		}
		// if ( font != null && !font.isDisposed() ) {
		// font.dispose();
		// }
		font = new Font(SwtGui.getDisplay(), data);
		minus.setEnabled(data.height >= 6);
		text.setFont(font);
		text.update();
		editor.updateBoxes();

	}

	private ToolItem getMarkItem() {
		return mark;
	}

	private ToolItem getLineItem() {
		return line;
	}

	private void registerListeners() {
		if ( listenersRegistered ) { return; }
		listenersRegistered = true;
		// Listening to "Mark occurrences..."
		final GamaPreferences.Entry<Boolean> pref = GamaPreferences.get("editor.mark.occurences", Boolean.class);
		final IPreferenceChangeListener<Boolean> change = new IPreferenceChangeListener<Boolean>() {

			@Override
			public boolean beforeValueChange(final Boolean newValue) {
				return true;
			}

			@Override
			public void afterValueChange(final Boolean newValue) {
				visitToolbars(new IToolbarVisitor() {

					@Override
					public void visit(final EditToolbar toolbar) {
						toolbar.getMarkItem().setSelection(newValue);
					}
				});
			}
		};
		pref.addChangeListener(change);
		// Listening to "Line number"
		IPreferenceStore store = editor.getAdvancedPreferenceStore();
		store.addPropertyChangeListener(new IPropertyChangeListener() {

			@Override
			public void propertyChange(final PropertyChangeEvent event) {
				String id = event.getProperty();
				IToolbarVisitor visitor = null;
				if ( id.equals(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_LINE_NUMBER_RULER) ) {
					visitor = new IToolbarVisitor() {

						@Override
						public void visit(final EditToolbar toolbar) {
							toolbar.getLineItem().setSelection((Boolean) event.getNewValue());
						}
					};
				}
				if ( visitor != null ) {
					visitToolbars(visitor);
				}
			}
		});
	}

	/**
	 *
	 */
	public void resetColorMenu() {
		EditToolbarMenu menu = EditToolbarMenuFactory.getInstance().getColorMenu();
		if ( menu != null ) {
			menu.reset();
		}
	}

	public void resetOperatorsMenu() {
		EditToolbarMenu menu = EditToolbarMenuFactory.getInstance().getOperatorsMenu();
		if ( menu != null ) {
			menu.reset();
		}
	}
}

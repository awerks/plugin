package org.awerks.plugin;


import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.event.CaretEvent;
import com.intellij.openapi.editor.event.CaretListener;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.Consumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.awt.event.MouseEvent;

public class PsiTypeStatusBarWidget implements StatusBarWidget, StatusBarWidget.TextPresentation {

    private final Project project;
    private String myText = "Type: Unknown";

    public PsiTypeStatusBarWidget(@NotNull Project project) {
        this.project = project;

        EditorFactory.getInstance().getEventMulticaster().addCaretListener(new CaretListener() {
            @Override
            public void caretPositionChanged(@NotNull CaretEvent e) {
                updateTypeAtCaret(e);
            }
        }, project);
    }

    private void updateTypeAtCaret(@NotNull CaretEvent e) {
        Editor editor = e.getEditor();
        if (editor.getProject() == null || !editor.getProject().equals(project)) {
            return;
        }

        // find the PsiFile for this editor.
        PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument());
        if (psiFile == null) {
            setTypeText("Unknown");
            return;
        }


        // get the psi element at the caret.
        int offset = editor.getCaretModel().getOffset();
        PsiElement element = psiFile.findElementAt(offset);
        if (element == null) {
            setTypeText("Unknown");
            return;
        }

        String tokenText = element.getText();
        String guessedType = inferTypeNaively(tokenText);

        setTypeText(guessedType);
    }

    private void setTypeText(@NotNull String newType) {
        myText = "Type: " + newType;
        StatusBar statusBar = com.intellij.openapi.wm.WindowManager.getInstance().getStatusBar(project);
        if (statusBar != null) {
            statusBar.updateWidget(ID());
        }
    }

    private String inferTypeNaively(String token) {
        if (token == null || token.isEmpty()) {
            return "Unknown";
        }
        token = token.trim();

        if ((token.startsWith("\"") && token.endsWith("\"")) ||
                (token.startsWith("'") && token.endsWith("'"))) {
            return "str";
        }

        if (token.equals("True") || token.equals("False")) {
            return "bool";
        }

        if (token.equals("None")) {
            return "NoneType";
        }

        if (token.matches("^-?\\d+$")) {
            return "int";
        }

        if (token.matches("^-?\\d+\\.\\d+$")) {
            return "float";
        }


        return "Unknown";
    }
    @NotNull
    @Override
    public String ID() {
        return "PsiTypeStatusBarWidget";
    }

    @Nullable
    @Override
    public WidgetPresentation getPresentation() {
        return this;
    }


    @Override
    public void dispose() {

    }


    @NotNull
    @Override
    public String getText() {
        return myText;
    }

    @Nullable
    @Override
    public String getTooltipText() {
        return "Displays the type extracted from the PSI element at the caret.";
    }

    @Override
    public @Nullable String getShortcutText() {
        return TextPresentation.super.getShortcutText();
    }

    @Override
    public Consumer<MouseEvent> getClickConsumer() {
        return TextPresentation.super.getClickConsumer();
    }

    @Override
    public void install(@NotNull StatusBar statusBar) {
        StatusBarWidget.super.install(statusBar);
    }

    @Override
    public @Nullable WidgetPresentation getPresentation(@NotNull StatusBarWidget.PlatformType type) {
        return StatusBarWidget.super.getPresentation(type);
    }

    @Override
    public float getAlignment() {
        return 0.0f;
    }
}
package seedu.address.ui;

import javafx.scene.Node;
import javafx.scene.layout.FlowPane;

/** A horizontal tag flow that measures and positions each row at the same constrained widths. */
public class TagFlowPane extends FlowPane {
    @Override
    protected double computePrefWidth(double height) {
        return snappedLeftInset() + getPrefWrapLength() + snappedRightInset();
    }

    @Override
    protected double computeMinHeight(double width) {
        return computePrefHeight(width);
    }

    @Override
    protected double computePrefHeight(double width) {
        double contentWidth = width < 0 ? getPrefWrapLength()
                : width - snappedLeftInset() - snappedRightInset();
        return arrangeTags(Math.max(0, contentWidth), false);
    }

    @Override
    protected void layoutChildren() {
        arrangeTags(Math.max(0, getWidth() - snappedLeftInset() - snappedRightInset()), true);
    }

    /** Computes row heights and optionally lays out tags using those same measurements. */
    private double arrangeTags(double availableWidth, boolean shouldLayout) {
        double horizontalGap = snapSpaceX(getHgap());
        double verticalGap = snapSpaceY(getVgap());
        double x = 0;
        double y = 0;
        double rowHeight = 0;
        boolean hasTagInRow = false;
        for (Node child : getManagedChildren()) {
            double width = Math.min(availableWidth, snapSizeX(child.prefWidth(-1)));
            double height = snapSizeY(child.prefHeight(width));
            if (hasTagInRow && x + width > availableWidth) {
                x = 0;
                y += rowHeight + verticalGap;
                rowHeight = 0;
            }
            if (shouldLayout) {
                child.resizeRelocate(snappedLeftInset() + x, snappedTopInset() + y, width, height);
            }
            x += width + horizontalGap;
            rowHeight = Math.max(rowHeight, height);
            hasTagInRow = true;
        }
        return snappedTopInset() + y + rowHeight + snappedBottomInset();
    }
}

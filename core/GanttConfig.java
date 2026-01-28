    public void setScaleType(ScaleType type) {
        this.scaleType = type;
        repaint();
    }

    public void setZoomFactor(double factor) {
        this.zoomFactor = Math.max(0.5, Math.min(factor, 3.0));
        repaint();
    }
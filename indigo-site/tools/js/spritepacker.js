/*
* Heavilly based on https://raw.githubusercontent.com/jakesgordon/bin-packing/master/js/packer.growing.js
* Modified to yield rather than mutate
*/
SpritePacker = function() { };

SpritePacker.prototype = {
    fit: function (blocks) {
        let n, node, block, len = blocks.length;
        const w = len > 0 ? blocks[0].w : 0;
        const h = len > 0 ? blocks[0].h : 0;
        this.root = { x: 0, y: 0, w: w, h: h };

        for (n = 0; n < len ; n++) {
            if (blocks[n].w === null || blocks[n].w === undefined || blocks[n].h === null || blocks[n].h === undefined)
                console.error('All blocks for sprite packing must contain w (width) and h (height) properties (error in element ' + n + ')');

            if (isNaN(blocks[n].w))
                console.error('Block must have numeric w property (error in element ' + n + ')');

            if (isNaN(blocks[n].h))
                console.error('Block must have numeric h property (error in element ' + n + ')');

            block = blocks[n]

            if (node = this.findNode(this.root, block.w, block.h))
                block.fit = this.splitNode(node, block.w, block.h);
            else
                block.fit = this.growNode(block.w, block.h);
        }

        return blocks;
    },

    findNode: function(root, w, h) {
        if (root.used)
            return this.findNode(root.right, w, h) || this.findNode(root.down, w, h);
        else if ((w <= root.w) && (h <= root.h))
            return root;
        else
            return null;
    },

    splitNode: function(node, w, h) {
        node.used = true;
        node.down  = { x: node.x,     y: node.y + h, w: node.w,     h: node.h - h };
        node.right = { x: node.x + w, y: node.y,     w: node.w - w, h: h          };
        return node;
    },

    growNode: function(w, h) {
        const canGrowDown  = (w <= this.root.w);
        const canGrowRight = (h <= this.root.h);

        const shouldGrowRight = canGrowRight && (this.root.h >= (this.root.w + w)); // attempt to keep square-ish by growing right when height is much greater than width
        const shouldGrowDown  = canGrowDown  && (this.root.w >= (this.root.h + h)); // attempt to keep square-ish by growing down  when width  is much greater than height

        if (shouldGrowRight)
            return this.growRight(w, h);
        else if (shouldGrowDown)
            return this.growDown(w, h);
        else if (canGrowRight)
            return this.growRight(w, h);
        else if (canGrowDown)
            return this.growDown(w, h);
        else
            return null; // need to ensure sensible root starting size to avoid this happening
    },

    growRight: function(w, h) {
        this.root = {
            used: true,
            x: 0,
            y: 0,
            w: this.root.w + w,
            h: this.root.h,
            down: this.root,
            right: { x: this.root.w, y: 0, w: w, h: this.root.h }
        };

        if (node = this.findNode(this.root, w, h))
            return this.splitNode(node, w, h);
        else
            return null;
    },

    growDown: function(w, h) {
        this.root = {
            used: true,
            x: 0,
            y: 0,
            w: this.root.w,
            h: this.root.h + h,
            down:  { x: 0, y: this.root.h, w: this.root.w, h: h },
            right: this.root
        };

        if (node = this.findNode(this.root, w, h))
            return this.splitNode(node, w, h);
        else
            return null;
    }
}

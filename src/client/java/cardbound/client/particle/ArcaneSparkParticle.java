package cardbound.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SimpleAnimatedParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;

public class ArcaneSparkParticle extends SimpleAnimatedParticle {

    private ArcaneSparkParticle(
            ClientLevel level,
            double x,
            double y,
            double z,
            double xa,
            double ya,
            double za,
            SpriteSet sprites
    ) {
        super(level, x, y, z, sprites, 0.0125F);

        this.xd = xa;
        this.yd = ya;
        this.zd = za;

        this.quadSize *= 0.75F;

        // The important difference from vanilla End Rod!
        this.lifetime = 5;

        this.setFadeColor(15916745);
        this.setSpriteFromAge(sprites);
    }

    @Override
    public void move(double xa, double ya, double za) {
        this.setBoundingBox(
                this.getBoundingBox().move(xa, ya, za)
        );
        this.setLocationFromBoundingbox();
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {

        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(
                SimpleParticleType options,
                ClientLevel level,
                double x,
                double y,
                double z,
                double xa,
                double ya,
                double za,
                RandomSource random
        ) {
            return new ArcaneSparkParticle(
                    level,
                    x, y, z,
                    xa, ya, za,
                    sprites
            );
        }
    }
}